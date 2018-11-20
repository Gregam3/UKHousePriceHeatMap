package asegroup1.api.services.landregistry;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable;


/**
 * @author Greg Mitten, Rikkey Paal
 * gregoryamitten@gmail.com
 */

//Does not need to extend ServiceImpl as does not use a Dao

@Service
public class LandRegistryServiceImpl {

    private LandRegistryDaoImpl postCodeCoordinatesDao;

    @Autowired
    public LandRegistryServiceImpl(LandRegistryDaoImpl postCodeCoordinatesDao) {
        this.postCodeCoordinatesDao = postCodeCoordinatesDao;
    }

    //API CONSTANTS
    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBGmy-uAlzvXRLcQ_krAaY0idR1KUTJRmA";
    private static final String LR_SPACE = "%20";

    //OTHER CONSTANTS
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int[] AGGREGATION_LEVELS = new int[]{100};

    public List<LandRegistryData> getAddressesForPostCode(String postCode) throws UnirestException {
        List<LandRegistryData> landRegistryDataList = new LinkedList<>();
        JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCode.replace(" ", LR_SPACE).toUpperCase())
                .asJson().getBody().getObject().getJSONObject("result").getJSONArray("items");

        for (int i = 0; i < addresses.length(); i++) {
            JSONObject currentNode = (JSONObject) addresses.get(i);

            LandRegistryData data = new LandRegistryData();
            data.setPrimaryHouseName(currentNode.get("paon").toString());
            data.setStreetName(currentNode.get("street").toString());
            data.setTownName(currentNode.get("town").toString());
            data.setPostCode(postCode);

            landRegistryDataList.add(data);
        }

        return getPositionForAddresses(landRegistryDataList);
    }


    public List<LandRegistryData> getTransactionsForPostCode(LandRegistryQueryConstraint values, LandRegistryQuerySelect select, boolean latestOnly)
            throws IOException, UnirestException {
        List<LandRegistryData> transactionsList = new LinkedList<>();

        String query = latestOnly ? buildUniqueQuery(select, values) : buildQuery(select, values);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) OBJECT_MAPPER.readTree(queryResponse.get("result").toString()).get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {
            transactionsList.add(new LandRegistryData(jsonNode));
        }

        return getPositionForAddresses(transactionsList);
    }


    public List<LandRegistryData> getTransactions(LandRegistryQueryConstraint values, boolean latestOnly) throws IOException, UnirestException {
        return getTransactionsForPostCode(values, new LandRegistryQuerySelect(true), latestOnly);
    }

    private List<String> fetchPostCodesInsideCoordinateBox(double top, double right, double bottom, double left) {
        return postCodeCoordinatesDao.searchForPostCodesInBoundaries(top, right, bottom, left);
    }

    public List<LandRegistryData> getPositionForLocations(JSONObject mapPosition) throws UnirestException {
        List<LandRegistryData> fetchedData = new ArrayList<>();

        List<String> postCodes = fetchPostCodesInsideCoordinateBox(
                mapPosition.getDouble("top"),
                mapPosition.getDouble("right"),
                mapPosition.getDouble("bottom"),
                mapPosition.getDouble("left")
        );

        List<LandRegistryData> landRegistryDataForPostcodes = getLandRegistryDataForPostcodes(postCodes);

        int postcodesContained = landRegistryDataForPostcodes.size();

        if (postcodesContained > 1000)
            //TODO IMPLEMENT RETURNING OF HEATMAP DATA
            throw new AssertionError("Not yet implemented for over 1000 postcodes");
        else if (postcodesContained > 4) {
            //TODO IMPLEMENT RETURNING OF AGGREGATED POSTCODE DATA
            throw new AssertionError("Not yet implemented for over 4 postcodes");
        } else {
            for (LandRegistryData landRegistryDataForPostcode : landRegistryDataForPostcodes) {
                fetchedData.addAll(getAddressesForPostCode(landRegistryDataForPostcode.getConstraint(Selectable.postcode)));
            }
        }

        return fetchedData;
    }

    private List<LandRegistryData> getLandRegistryDataForPostcodes(List<String> postcodes) {
        StringBuilder constraintQueryBuilder = new StringBuilder("WHERE ");

        for (String postcode : postcodes) {
            constraintQueryBuilder
                    .append("postcode = '")
                    .append(postcode)
                    .append("' OR \n\t ");
        }

        return postCodeCoordinatesDao.getLandRegistryDataByPostcode(constraintQueryBuilder.substring(0, constraintQueryBuilder.length() - 7));
    }

    private List<LandRegistryData> getPositionsForPostCodes(List<LandRegistryData> addresses) {
        List<String> postcodeLocationDataList = new ArrayList<>();
        StringBuilder constraintQueryBuilder = new StringBuilder("WHERE ");

        for (LandRegistryData address : addresses)
            if (!postcodeLocationDataList.contains(address.getConstraint(Selectable.postcode))) {
                postcodeLocationDataList.add(address.getConstraint(Selectable.postcode));
                constraintQueryBuilder
                        .append("postcode = '")
                        .append(address.getConstraint(Selectable.postcode))
                        .append("' OR \n\t ");
            }

        //substring removes final OR and new line value
        return postCodeCoordinatesDao.getLandRegistryDataByPostcode(constraintQueryBuilder.substring(0, constraintQueryBuilder.length() - 7));
    }

    public List<LandRegistryData> getPositionForAddresses(List<LandRegistryData> addresses) {
        if (addresses.size() >= 100) {
            throw new InvalidParameterException("This method should never be passed more than 100 addresses");
        }

        StringBuilder addressUriBuilder = new StringBuilder();

        for (LandRegistryData address : addresses) {
            addressUriBuilder

                    .append(GOOGLE_MAPS_URL)
                    .append(address.getConstraintNotNull(Selectable.paon).replace(" ", "+"))
                    .append("+")
                    .append(address.getConstraintNotNull(Selectable.street).replace(" ", "+"))
                    .append("+")
                    .append(address.getConstraintNotNull(Selectable.town).replace(" ", "+"))
                    .append("&key=")
                    .append(GOOGLE_MAPS_API_KEY);

            try {
                JSONObject response = Unirest.get(addressUriBuilder.toString())
                        .asJson()
                        .getBody()
                        .getArray()
                        .getJSONObject(0)
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");

                address.setLatitude(response.getDouble("lat"));
                address.setLongitude(response.getDouble("lng"));

            } catch (UnirestException | JSONException e) {
                e.printStackTrace();
                System.err.println("Could not retrieve address for " + addressUriBuilder.toString());
            }

            //Clear the StringBuilder buffer
            addressUriBuilder.delete(0, addressUriBuilder.length());
        }

        return addresses;
    }

    private JSONObject executeSPARQLQuery(String query) throws UnirestException {
        //Navigates through JSON and returns list of addresses based on post code
        return Unirest.post(LAND_REGISTRY_SPARQL_ENDPOINT)
                .field("output", "json")
                .field("q", query)
                .field("url", "/landregistry/query")
                .asJson()
                .getBody()
                .getObject();
    }

    private String buildQuery(LandRegistryQuerySelect select, LandRegistryQueryConstraint values) {
        return getQueryPrefixDeclarations() + "\n" + select.buildQuerySelect() + "\n" + values.buildQueryWhere();
    }

    private String buildUniqueQuery(LandRegistryQuerySelect select, LandRegistryQueryConstraint values) {
        return getQueryPrefixDeclarations() + "\n" + select.buildQuerySelectUnique() + "\n" + values.buildQueryWhere() + values.buildUniqueGrouping();
    }

    private String getQueryPrefixDeclarations() {
        return "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                + "prefix owl: <http://www.w3.org/2002/07/owl#> \n" + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n"
                + "prefix sr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/> \n" + "prefix ukhpi: <http://landregistry.data.gov.uk/def/ukhpi/> \n"
                + "prefix lrppi: <http://landregistry.data.gov.uk/def/ppi/> \n" + "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n"
                + "prefix lrcommon: <http://landregistry.data.gov.uk/def/common/>";
    }
}