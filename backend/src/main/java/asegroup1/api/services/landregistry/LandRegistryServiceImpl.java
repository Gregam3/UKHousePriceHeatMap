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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
    private static final int[] AGGREGATION_LEVELS = new int[]{0, 15, 8000};


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

        return transactionsList;
    }

    public List<LandRegistryData> getTransactions(LandRegistryQuerySelect select, LandRegistryQueryConstraint constraint, LandRegistryQueryGroup group)
            throws IOException, UnirestException {
        return getTransactions(new LandRegistryQuery(constraint, group, select));
    }

    public List<LandRegistryData> getTransactions(LandRegistryQuerySelect select, LandRegistryQueryConstraint constraint) throws IOException, UnirestException {
        return getTransactions(new LandRegistryQuery(constraint, null, select));
    }

    public List<LandRegistryData> getLatestTransactions(List<Selectable> values, LandRegistryQueryConstraint constraint) throws IOException, UnirestException {
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

        if (postcodesContained > AGGREGATION_LEVELS[2]) {
            return convertLandRegistryDataListToHeatMapList(landRegistryDataForPostcodes);
        } else if (postcodesContained > AGGREGATION_LEVELS[1]) {
            return landRegistryDataForPostcodes;
        } else if (postcodesContained > AGGREGATION_LEVELS[0]) {
            LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
            constraint.setMinDate(LocalDate.now().minusYears(LandRegistryData.YEARS_TO_FETCH));

            List<String> postcodes = new ArrayList<>();

            for (LandRegistryData landRegistryDataForPostcode : landRegistryDataForPostcodes)
                postcodes.add(landRegistryDataForPostcode.getConstraint(Selectable.postcode));

            constraint.setEqualityConstraint(Selectable.postcode, postcodes.toArray(new String[0]));

            return getPositionForAddresses(
                    getTransactions(
                            LandRegistryQuery.buildQueryLatestSalesOnly(constraint, Arrays.asList(
                                    Selectable.paon,
                                    Selectable.street,
                                    Selectable.town,
                                    Selectable.pricePaid
                            ))
                    ));
        } else {
            return new ArrayList<>();
        }
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

    public static int[] getAggregationLevels() {
        return AGGREGATION_LEVELS;
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

        Random random = new Random();

        //Find the minimum and maximum price, this is needed to normalise the values
        long min, max;
        min = max = random.nextInt(10000000); //Long.parseLong(landRegistryDataList.get(0).getConstraint(Selectable.pricePaid));

        List<Long> pricesPaid = new ArrayList<>();

        for (int i = 1; i < landRegistryDataList.size(); i++) {
            long pricePaid = random.nextInt(10000000); // Long.parseLong(landRegistryDataList.get(i).getConstraint(Selectable.pricePaid));

            pricesPaid.add(pricePaid);

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
//                    (double) (Long.parseLong(landRegistryDataList.get(i).getConstraint(Selectable.pricePaid)) - min) / (double) (max - min)
                    (double) (pricesPaid.get((i < 1) ? i : i - 1) - min) / (double) (max - min)
                    )
            );
        }

        return heatMapDataPoints;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private Colour getColoursForNormalisedValues(Double normalisedValue) {
        //The higher the normalised value the darker the red will appear
        return new Colour(255 - (int) (normalisedValue * 200));
    }
}