package asegroup1.api.services.landregistry;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.Address;
import asegroup1.api.models.AddressWithTransaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    private LandRegistryDaoImpl postCodeCoordinatesDao;

    @Autowired
    public LandRegistryServiceImpl(LandRegistryDaoImpl postCodeCoordinatesDao) throws IOException {
        this.postCodeCoordinatesDao = postCodeCoordinatesDao;
        Properties queries = new Properties();
        queries.load(new FileInputStream("src/main/java/asegroup1/api/services/landregistry/queries.properties"));

        transactionQuery = queries.getProperty("transactions-post-code-query");
    }

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String SPACE = "%20";
    private String transactionQuery;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public List<Address> getAddressesByPostCode(String postCode) throws UnirestException, IOException {

        List<Address> addressList = new LinkedList<>();
        JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCode.replace(" ", SPACE).toUpperCase())
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

        return addressList;
    }

    public List<AddressWithTransaction> getTransactionsByPostCode(String postcode) throws IOException, UnirestException, ParseException {
        List<AddressWithTransaction> transactionsList = new LinkedList<>();

        String query = transactionQuery.replace("REPLACETHIS", postcode);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) OBJECT_MAPPER.readTree(
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

        return transactionsList;
    }

    public List<String> fetchPostCodesInsideCoordinateBox(double top, double right, double bottom, double left) {
        return postCodeCoordinatesDao.searchForPostCodesInBoundaries(top, right, bottom, left);
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
}