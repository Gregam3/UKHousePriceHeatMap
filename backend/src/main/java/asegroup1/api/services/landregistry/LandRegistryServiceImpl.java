package asegroup1.api.services.landregistry;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.heatmap.HeatMapDataPoint;
import asegroup1.api.models.landregistry.*;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


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

    public List<LandRegistryData> getTransactions(LandRegistryQuery query)
            throws IOException, UnirestException {
        List<LandRegistryData> transactionsList = new LinkedList<>();

        String queryStr = query.buildQuery();

        JSONObject queryResponse = executeSPARQLQuery(queryStr);

        ArrayNode transactionListResponse = (ArrayNode) OBJECT_MAPPER.readTree(queryResponse.get("result").toString()).get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {
            transactionsList.add(new LandRegistryData(jsonNode));
        }

        return getPositionForAddresses(transactionsList);
    }

    public List<LandRegistryData> getTransactions(LandRegistryQuerySelect select, LandRegistryQueryConstraint constraint, LandRegistryQueryGroup group)
            throws IOException, UnirestException {
        return getTransactions(new LandRegistryQuery(constraint, group, select));
    }

    public List<LandRegistryData> getTransactions(LandRegistryQuerySelect select, LandRegistryQueryConstraint constraint) throws IOException, UnirestException {
        return getTransactions(new LandRegistryQuery(constraint, null, select));
    }

    public List<LandRegistryData> getLatestTransactions(List<Selectable> values, LandRegistryQueryConstraint constraint) throws IOException, UnirestException, ParseException {
        return getTransactions(LandRegistryQuery.buildQueryLatestSalesOnly(constraint, values));
    }

    public List<LandRegistryData> getLatestTransactions(LandRegistryQueryConstraint constraint) throws IOException, UnirestException, ParseException {
        return getLatestTransactions(new ArrayList<Selectable>(), constraint);
    }

    private List<LandRegistryData> fetchPostCodesInsideCoordinateBox(double top, double right, double bottom, double left) {
        return postCodeCoordinatesDao.searchForLandRegistryDataInBoundaries(top, right, bottom, left);
    }

    public List<?> getPositionInsideBounds(JSONObject mapPosition) throws UnirestException, IOException {
        List<LandRegistryData> fetchedData = new ArrayList<>();

        List<LandRegistryData> landRegistryDataForPostcodes = fetchPostCodesInsideCoordinateBox(
                mapPosition.getDouble("top"),
                mapPosition.getDouble("right"),
                mapPosition.getDouble("bottom"),
                mapPosition.getDouble("left")
        );

        int postcodesContained = landRegistryDataForPostcodes.size();

        if (postcodesContained > 10000) {
            return convertLandRegistryDataListToHeatMapList(landRegistryDataForPostcodes);
        } else if (postcodesContained > 4) {
            return landRegistryDataForPostcodes;
        } else {
            LandRegistryQuerySelect landRegistryQuerySelect = new LandRegistryQuerySelect(Selectable.paon, Selectable.pricePaid);

            LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
            constraint.setMinDate(LocalDate.now().minusYears(5));

            StringBuilder queryPrefix = new StringBuilder("prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "prefix owl: <http://www.w3.org/2002/07/owl#> \n" +
                    "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix sr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/> \n" +
                    "prefix ukhpi: <http://landregistry.data.gov.uk/def/ukhpi/> \n" +
                    "prefix lrppi: <http://landregistry.data.gov.uk/def/ppi/> \n" +
                    "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n" +
                    "prefix lrcommon: <http://landregistry.data.gov.uk/def/common/>\n" +
                    "SELECT ?paon ?saon ?street ?postcode (MAX(?transactionDate) AS ?TransactionDate) (SAMPLE(?pricePaid) AS ?PricePaid)\n" +
                    "WHERE { \n" +
                    "      VALUES (?POSTCODE) {");

            for (LandRegistryData landRegistryDataForPostcode : landRegistryDataForPostcodes) {
                queryPrefix
                        .append("(\"")
                        .append(landRegistryDataForPostcode.getConstraint(Selectable.postcode))
                        .append("\") ");
            }

            queryPrefix.append("} ?addr lrcommon:postcode ?POSTCODE.\n" +
                    "    ?transx lrppi:propertyAddress ?addr ; \n" +
                    "        lrppi:propertyType/skos:prefLabel ?propertyType ; \n" +
                    "        lrppi:estateType/skos:prefLabel ?estateType ; \n" +
                    "        lrppi:transactionDate ?transactionDate ; \n" +
                    "        lrppi:pricePaid ?pricePaid ; \n" +
                    "        lrppi:newBuild ?newBuild ; \n" +
                    "        lrppi:transactionCategory/skos:prefLabel ?transactionCategory.\n" +
                    "    OPTIONAL {?addr lrcommon:paon ?paon} \n" +
                    "    OPTIONAL {?addr lrcommon:saon ?saon} \n" +
                    "    OPTIONAL {?addr lrcommon:street ?street} \n" +
                    "    OPTIONAL {?addr lrcommon:locality ?locality} \n" +
                    "    OPTIONAL {?addr lrcommon:town ?town} \n" +
                    "    OPTIONAL {?addr lrcommon:district ?district} \n" +
                    "    OPTIONAL {?addr lrcommon:county ?county} \n" +
                    "    OPTIONAL {?addr lrcommon:postcode ?postcode}\n" +
                    "    \n" +
                    "    FILTER (\n" +
                    "        ?transactionDate > \"2013-11-21\"^^xsd:date\n" +
                    "    )\n" +
                    "}\n" +
                    "GROUP BY ?paon ?saon ?street ?postcode");

            executeSPARQLQuery(queryPrefix.toString());
        }

        return getPositionForAddresses(fetchedData);
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

        for (LandRegistryData address : addresses) {
            addressUriBuilder.append(GOOGLE_MAPS_URL).append(address.getConstraintNotNull(Selectable.paon).replace(" ", "+")).append("+")
                    .append(address.getConstraintNotNull(Selectable.street).replace(" ", "+")).append("+").append(address.getConstraintNotNull(Selectable.town).replace(" ", "+"))
                    .append("&key=").append(GOOGLE_MAPS_API_KEY);

            try {
                JSONObject response = Unirest.get(addressUriBuilder.toString()).asJson().getBody().getArray().getJSONObject(0).getJSONArray("results").getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location");

                address.setLatitude(response.getDouble("lat"));
                address.setLongitude(response.getDouble("lng"));

            } catch (UnirestException | JSONException e) {
                e.printStackTrace();
                System.err.println("Could not retrieve address for " + addressUriBuilder.toString());
            }

            // Clear the StringBuilder buffer
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

    public List<HeatMapDataPoint> convertLandRegistryDataListToHeatMapList(List<LandRegistryData> landRegistryDataList) {
        if (landRegistryDataList.isEmpty()) {
            return null;
        }

        //Find the minimum and maximum price, this is needed to normalise the values
        long min, max;
        min = max = Long.parseLong(landRegistryDataList.get(0).getConstraint(Selectable.pricePaid));

        for (int i = 1; i < landRegistryDataList.size(); i++) {
            long pricePaid = Long.parseLong(landRegistryDataList.get(i).getConstraint(Selectable.pricePaid));

            if (pricePaid > max) max = pricePaid;
            if (pricePaid < min) min = pricePaid;
        }

        //Convert list of LandRegistryData to list of HeatMapDataPoints
        List<HeatMapDataPoint> heatMapDataPoints = landRegistryDataList.parallelStream().map(lr ->
                new HeatMapDataPoint(
                        lr.getLatitude(),
                        lr.getLongitude(),
                        null
                )).collect(Collectors.toList());

        for (int i = 0; i < landRegistryDataList.size() && i < heatMapDataPoints.size(); i++) {
            //Pass in the normalised value and receive a Colour object
            heatMapDataPoints.get(i).setColour(getColoursForNormalisedValues(
                    //Normalise the values between 0 and 1.0
                    (double) (Long.parseLong(landRegistryDataList.get(i).getConstraint(Selectable.pricePaid)) - min) / (double) (max - min)
                    )
            );
        }

        return heatMapDataPoints;
    }

    private Colour getColoursForNormalisedValues(Double normalisedValue) {
        //The higher the normalised value the darker the red will appear
        return new Colour(255 - (int) (normalisedValue * 200));
    }

}