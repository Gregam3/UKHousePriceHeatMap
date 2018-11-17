package asegroup1.api.services.landregistry;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.heatmap.HeatMapDataPoint;
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


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    private LandRegistryDaoImpl postCodeCoordinatesDao;

    @Autowired
    public LandRegistryServiceImpl(LandRegistryDaoImpl postCodeCoordinatesDao) throws IOException {
        this.postCodeCoordinatesDao = postCodeCoordinatesDao;
    }

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String OPEN_STREET_MAP_URL_PREFIX = "https://nominatim.openstreetmap.org/search/";
    private static final String OPEN_STREET_MAP_URL_SUFFIX = "?format=json&addressdetails=1&limit=1&polygon_svg=1";
    //    private static final String GOOGLE_GEOCODE_API_KEY = "AIzaSyAdX29NBwzTwVEM9K-eLnDx86Al-yHGRqQ";
    private static final String LR_SPACE = "%20";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<LandRegistryData> getAddressesForPostCode(String postCode) throws UnirestException, IOException {
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
            throws IOException, UnirestException, ParseException {
        List<LandRegistryData> transactionsList = new LinkedList<>();

        String query = latestOnly ? buildUniqueQuery(select, values) : buildQuery(select, values);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) OBJECT_MAPPER.readTree(queryResponse.get("result").toString()).get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {
            transactionsList.add(new LandRegistryData(jsonNode));
        }

        return getPositionForAddresses(transactionsList);
    }

    public List<LandRegistryData> getTransactions(LandRegistryQueryConstraint values, boolean latestOnly) throws IOException, UnirestException, ParseException {
        return getTransactionsForPostCode(values, new LandRegistryQuerySelect(true), latestOnly);
    }

    public List<String> fetchPostCodesInsideCoordinateBox(double top, double right, double bottom, double left) {
        return postCodeCoordinatesDao.searchForPostCodesInBoundaries(top, right, bottom, left);
    }

    public List<LandRegistryData> getPositionForAddresses(List<LandRegistryData> addresses) {
        StringBuilder addressUriBuilder = new StringBuilder();

        for (LandRegistryData address : addresses) {
            addressUriBuilder
                    .append(OPEN_STREET_MAP_URL_PREFIX)
                    .append(address.getConstraintNotNull(Selectable.paon))
                    .append(" ")
                    .append(address.getConstraintNotNull(Selectable.street))
                    .append(" ")
                    .append(address.getConstraintNotNull(Selectable.town));
            addressUriBuilder.append(OPEN_STREET_MAP_URL_SUFFIX);

            try {
                JSONObject response = Unirest.get(
                        addressUriBuilder.toString()
                ).asJson().getBody().getArray().getJSONObject(0);

                address.setLatitude(response.getDouble("lat"));
                address.setLongitude(response.getDouble("lon"));

            } catch (UnirestException | JSONException e) {
                e.printStackTrace();
                System.err.println("Could not retrieve address for " + addressUriBuilder.toString());
            }

            //Clear the StringBuilder buffer
            addressUriBuilder.delete(0, addressUriBuilder.length());
        }

        return addresses;
    }

    private JSONObject executeSPARQLQuery(String query) throws UnirestException, IOException {
        //Navigates through JSON and returns list of addresses based on post code
        return Unirest.post(LAND_REGISTRY_SPARQL_ENDPOINT)
                .field("output", "json")
                .field("q", query)
                .field("url", "/landregistry/query")
                .asJson()
                .getBody()
                .getObject();
    }

    public List<Double> normaliseValues(List<Long> prices) {
        if(prices.isEmpty()) {
            return null;
        }

        List<Double> normalisedValues = new ArrayList<>();

        long min, max;
        min = max = prices.get(0);

        for (int i = 1; i < prices.size(); i++) {
            if (prices.get(i) > max) max = prices.get(i);
            if (prices.get(i) < min) min = prices.get(i);
        }

        if(max == min) {
            return prices.stream().map(p -> 0.0).collect(Collectors.toList());
        }

        for (Long price : prices) {
            normalisedValues.add((double) (price - min) / (double) (max - min));
        }

        return normalisedValues;
    }

    public List<Colour> getColoursForNormalisedValues(List<Double> normalisedValues) {
        //The higher the normalised value the darker the red will appear
        return normalisedValues.stream().map(v -> new Colour(255 - (int) (v * 200))).collect(Collectors.toList());
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