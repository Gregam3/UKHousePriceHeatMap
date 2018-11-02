package asegroup1.api.services.landregistry;

import asegroup1.api.models.HouseTransactionData;
import asegroup1.api.models.LandRegistryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String SPACE = "%20";

    public List<LandRegistryData> getLandRegistryDataByPostCode(String postCode) throws UnirestException, IOException {
        List<LandRegistryData> landRegistryDataList = new LinkedList<>();
        String[] postCodeSplit = postCode.split(" ");

        JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCodeSplit[0] + SPACE + postCodeSplit[1])
                .asJson().getBody().getObject().getJSONObject("result").getJSONArray("items");

        for (int i = 0; i < addresses.length(); i++) {
            JSONObject currentNode = (JSONObject) addresses.get(i);

            landRegistryDataList.add(
                    new LandRegistryData(
                            0,
                            new Date(0),
                            currentNode.get("paon").toString(),
                            currentNode.get("street").toString(),
                            currentNode.get("town").toString(),
                            postCode
                    )
            );
        }

        return landRegistryDataList;
    }

    public ArrayNode getTransactionsForPostCode(String postcode) throws IOException, UnirestException {
        //TODO find better way to store query string
        String postcodeQuery = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "prefix lrcommon: <http://landregistry.data.gov.uk/def/common/>\n" +
                "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
                "prefix sr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/>\n" +
                "prefix ukhpi: <http://landregistry.data.gov.uk/def/ukhpi/>\n" +
                "prefix lrppi: <http://landregistry.data.gov.uk/def/ppi/>\n" +
                "\n" +
                "# Returns the Price Paid data from the default graph for each transaction record having\n" +
                "# an address with the given postcode.\n" +
                "# The postcode to query is set using SPARQL 1.1's 'values' clause\n" +
                "\n" +
                "SELECT ?paon ?saon ?street ?town ?county ?postcode ?amount ?date ?category\n" +
                "WHERE\n" +
                "{\n" +
                "  VALUES ?postcode {\"" + postcode + "\"^^xsd:string}\n" +
                "\n" +
                "  ?addr lrcommon:postcode ?postcode.\n" +
                "\n" +
                "  ?transx lrppi:propertyAddress ?addr ;\n" +
                "          lrppi:pricePaid ?amount ;\n" +
                "          lrppi:transactionDate ?date ;\n" +
                "          lrppi:transactionCategory/skos:prefLabel ?category.\n" +
                "\n" +
                "  OPTIONAL {?addr lrcommon:county ?county}\n" +
                "  OPTIONAL {?addr lrcommon:paon ?paon}\n" +
                "  OPTIONAL {?addr lrcommon:saon ?saon}\n" +
                "  OPTIONAL {?addr lrcommon:street ?street}\n" +
                "  OPTIONAL {?addr lrcommon:town ?town}\n" +
                "}\n" +
                "ORDER BY ?amount";

        JSONObject queryResponse = executeSPARQLQuery(postcodeQuery);

        return  (ArrayNode) new ObjectMapper().readTree(
                queryResponse.get("result").toString())
                .get("results").get("bindings");

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
