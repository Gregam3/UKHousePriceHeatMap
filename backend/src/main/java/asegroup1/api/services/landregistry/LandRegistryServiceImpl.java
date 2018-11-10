package asegroup1.api.services.landregistry;

import asegroup1.api.models.Address;
import asegroup1.api.models.AddressWithTransaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    public LandRegistryServiceImpl() throws IOException {
        Properties queries = new Properties();
        queries.load(new FileInputStream("src/main/java/asegroup1/api/services/landregistry/queries.properties"));

        transactionQuery = queries.getProperty("transactions-post-code-query");
    }

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String OPEN_STREET_MAP_URL_PREFIX = "https://nominatim.openstreetmap.org/search/";
    private static final String OPEN_STREET_MAP_URL_SUFFIX = "?format=json&addressdetails=1&limit=1&polygon_svg=1";
    //    private static final String GOOGLE_GEOCODE_API_KEY = "AIzaSyAdX29NBwzTwVEM9K-eLnDx86Al-yHGRqQ";
    private static final String LR_SPACE = "%20";
    private static final String GOOGLE_SPACE = "+";
    private String transactionQuery;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public List<Address> getAddressesForPostCode(String postCode) throws UnirestException, IOException {
        List<Address> addressList = new LinkedList<>();
        JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCode.replace(" ", LR_SPACE).toUpperCase())
                .asJson().getBody().getObject().getJSONObject("result").getJSONArray("items");

        for (int i = 0; i < addresses.length(); i++) {
            JSONObject currentNode = (JSONObject) addresses.get(i);

            addressList.add(
                    new Address(
                            currentNode.get("paon").toString(),
                            currentNode.get("street").toString(),
                            currentNode.get("town").toString(),
                            postCode
                    )
            );
        }

        return getPositionForAddresses(addressList);
    }

    public List<Address> getTransactionsForPostCode(String postcode) throws IOException, UnirestException, ParseException {
        List<Address> transactionsList = new LinkedList<>();

        String query = transactionQuery.replace("REPLACETHIS", postcode);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) objectMapper.readTree(
                queryResponse.get("result").toString())
                .get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {
            ObjectNode currentNode = (ObjectNode) jsonNode;

            transactionsList.add(
                    new AddressWithTransaction(
                            currentNode.get("paon").get("value").asText(),
                            currentNode.get("street").get("value").asText(),
                            currentNode.get("town").get("value").asText(),
                            currentNode.get("postcode").get("value").asText(),
                            DATE_FORMAT.parse(currentNode.get("date").get("value").asText()),
                            currentNode.get("amount").get("value").asLong()
                    )
            );
        }

        return getPositionForAddresses(transactionsList);
    }

    public List<Address> getPositionForAddresses(List<Address> addresses) {
        StringBuilder addressUriBuilder = new StringBuilder();

        for (Address address : addresses) {
            addressUriBuilder
                    .append(OPEN_STREET_MAP_URL_PREFIX)
                    .append(address.getHouseName())
                    .append(" ")
                    .append(address.getStreetName())
                    .append(" ")
                    .append(address.getTownName());

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
}