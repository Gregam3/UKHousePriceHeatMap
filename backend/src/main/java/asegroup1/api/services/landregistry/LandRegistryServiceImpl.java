package asegroup1.api.services.landregistry;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect;


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    public LandRegistryServiceImpl() throws IOException {
        Properties queries = new Properties();
        queries.load(new FileInputStream("src/main/java/asegroup1/api/services/landregistry/queries.properties"));

    }

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String SPACE = "%20";
    private static final ObjectMapper objectMapper = new ObjectMapper();


	public List<HashMap<String, String>> getLandRegistryDataByPostCode(String postCode) throws UnirestException, IOException {
		List<HashMap<String, String>> landRegistryDataList = new LinkedList<>();
		JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCode.replace(" ", SPACE).toUpperCase())
                .asJson().getBody().getObject().getJSONObject("result").getJSONArray("items");

        for (int i = 0; i < addresses.length(); i++) {
            JSONObject currentNode = (JSONObject) addresses.get(i);

			LandRegistryData data = new LandRegistryData();
			data.setPrimaryHouseName(currentNode.get("paon").toString());
			data.setStreetName(currentNode.get("street").toString());
			data.setTownName(currentNode.get("town").toString());
			data.setPostCode(postCode);

			landRegistryDataList.add(data.getMappings());
        }

        return landRegistryDataList;
	}

	public List<HashMap<String, String>> getTransactions(LandRegistryQueryConstraint values, LandRegistryQuerySelect select, boolean latestOnly)
			throws IOException, UnirestException, ParseException {
		List<HashMap<String, String>> transactionsList = new LinkedList<>();
		
		String query = latestOnly ? buildUniqueQuery(select, values) : buildQuery(select, values);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) objectMapper.readTree(
                queryResponse.get("result").toString())
                .get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {

			transactionsList.add(new LandRegistryData(jsonNode).getMappings());
        }

        return transactionsList;
    }

	public List<HashMap<String, String>> getTransactions(LandRegistryQueryConstraint values, boolean latestOnly) throws IOException, UnirestException, ParseException {
		return getTransactions(values, new LandRegistryQuerySelect(true), latestOnly);
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
