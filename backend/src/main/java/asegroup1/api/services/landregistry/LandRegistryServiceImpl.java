package asegroup1.api.services.landregistry;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQueryGroup;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect;


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    private LandRegistryDaoImpl postCodeCoordinatesDao;

    @Autowired
    public LandRegistryServiceImpl(LandRegistryDaoImpl postCodeCoordinatesDao) {
        this.postCodeCoordinatesDao = postCodeCoordinatesDao;
    }

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBGmy-uAlzvXRLcQ_krAaY0idR1KUTJRmA";
    private static final String LR_SPACE = "%20";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


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

    public List<String> fetchPostCodesInsideCoordinateBox(double top, double right, double bottom, double left) {
        return postCodeCoordinatesDao.searchForPostCodesInBoundaries(top, right, bottom, left);
    }

    public List<LandRegistryData> getPositionForAddresses(List<LandRegistryData> addresses) {
//        StringBuilder addressUriBuilder = new StringBuilder();
//
//        for (LandRegistryData address : addresses) {
//            addressUriBuilder
//                    .append(GOOGLE_MAPS_URL)
//                    .append(address.getConstraintNotNull(Selectable.paon).replace(" ", "+"))
//                    .append("+")
//                    .append(address.getConstraintNotNull(Selectable.street).replace(" ", "+"))
//                    .append("+")
//                    .append(address.getConstraintNotNull(Selectable.town).replace(" ", "+"))
//                    .append("&key=")
//                    .append(GOOGLE_MAPS_API_KEY);
//
//            try {
//                JSONObject response = Unirest.get(addressUriBuilder.toString())
//                        .asJson()
//                        .getBody()
//                        .getArray()
//                        .getJSONObject(0)
//                        .getJSONArray("results")
//                        .getJSONObject(0)
//                        .getJSONObject("geometry")
//                        .getJSONObject("location");
//
//                address.setLatitude(response.getDouble("lat"));
//                address.setLongitude(response.getDouble("lng"));
//
//            } catch (UnirestException | JSONException e) {
//                e.printStackTrace();
//                System.err.println("Could not retrieve address for " + addressUriBuilder.toString());
//            }
//
//            //Clear the StringBuilder buffer
//            addressUriBuilder.delete(0, addressUriBuilder.length());
//        }

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

}