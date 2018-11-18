package asegroup1.api.services.landregistry;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import asegroup1.api.models.landregistry.LandRegistryQuery;
import asegroup1.api.models.landregistry.LandRegistryQuery.Aggrigation;
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
    private static final String OPEN_STREET_MAP_URL_PREFIX = "https://nominatim.openstreetmap.org/search/";
    private static final String OPEN_STREET_MAP_URL_SUFFIX = "?format=json&addressdetails=1&limit=1&polygon_svg=1";
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
		LandRegistryQueryGroup group = new LandRegistryQueryGroup(Selectable.paon, Selectable.saon, Selectable.street, Selectable.postcode);

		LandRegistryQuerySelect select = new LandRegistryQuerySelect();
		select.addSelectValue(Selectable.paon, Aggrigation.NONE);
		values.remove(Selectable.paon);
		select.addSelectValue(Selectable.saon, Aggrigation.NONE);
		values.remove(Selectable.saon);
		select.addSelectValue(Selectable.street, Aggrigation.NONE);
		values.remove(Selectable.street);
		select.addSelectValue(Selectable.postcode, Aggrigation.NONE);
		values.remove(Selectable.postcode);
		select.addSelectValue(Selectable.transactionDate, Aggrigation.MAX);
		values.remove(Selectable.transactionDate);

		for (Selectable selectable : values) {
			select.addSelectValue(selectable, Aggrigation.SAMPLE);
		}

		return getTransactions(select, constraint, group);
	}

	public List<LandRegistryData> getLatestTransactions(LandRegistryQueryConstraint constraint) throws IOException, UnirestException, ParseException {
		return getLatestTransactions(new ArrayList<Selectable>(), constraint);
	}

    public List<String> fetchPostCodesInsideCoordinateBox(double top, double right, double bottom, double left) {
        return postCodeCoordinatesDao.searchForPostCodesInBoundaries(top, right, bottom, left);
    }
  
	public List<LandRegistryData> getPositionForAddresses(List<LandRegistryData> addresses) {
		StringBuilder addressUriBuilder = new StringBuilder();

		for (LandRegistryData address : addresses) {
			addressUriBuilder.append(OPEN_STREET_MAP_URL_PREFIX).append(address.getConstraintNotNull(Selectable.paon)).append(" ")
					.append(address.getConstraintNotNull(Selectable.street)).append(" ").append(address.getConstraintNotNull(Selectable.town));
			addressUriBuilder.append(OPEN_STREET_MAP_URL_SUFFIX);

			try {
				JSONObject response = Unirest.get(addressUriBuilder.toString()).asJson().getBody().getArray().getJSONObject(0);

				address.setLatitude(response.getDouble("lat"));
				address.setLongitude(response.getDouble("lon"));

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

	public static void main(String[] args) {
		LandRegistryData equalityConstraints = new LandRegistryData();
		equalityConstraints.setTownName("Eastbourne");

		LandRegistryQueryConstraint values = new LandRegistryQueryConstraint(equalityConstraints);
		values.setMinDate(LocalDate.now().minusYears(5));

		try {
			System.out.println(new LandRegistryServiceImpl(null).getLatestTransactions(new ArrayList<>(), values));
		} catch (IOException | UnirestException | ParseException e) {
			e.printStackTrace();
		}
	}

}