package asegroup1.api.services.landregistry;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.models.LandRegistryData;
import asegroup1.api.models.LandRegistryDataWithTransaction;
import asegroup1.api.models.LandRegistryQueryConstraint;
import asegroup1.api.models.LandRegistryQuerySelect;
import asegroup1.api.models.LandRegistryQuerySelect.Selectable;


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
    private static final String SPACE = "%20";
    private String transactionQuery;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper objectMapper = new ObjectMapper();


	public List<LandRegistryData> getLandRegistryDataByPostCode(String postCode) throws UnirestException, IOException {
        List<LandRegistryData> landRegistryDataList = new LinkedList<>();
		JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCode.replace(" ", SPACE).toUpperCase())
                .asJson().getBody().getObject().getJSONObject("result").getJSONArray("items");

        for (int i = 0; i < addresses.length(); i++) {
            JSONObject currentNode = (JSONObject) addresses.get(i);

            landRegistryDataList.add(
                    new LandRegistryData(
                            currentNode.get("paon").toString(),
                            currentNode.get("street").toString(),
                            currentNode.get("town").toString(),
							postCode
                    )
            );
        }

        return landRegistryDataList;
	}

	public List<LandRegistryData> getTransactionsForPostCode(LandRegistryQueryConstraint values) throws IOException, UnirestException, ParseException {
        List<LandRegistryData> transactionsList = new LinkedList<>();
		LandRegistryQuerySelect select = new LandRegistryQuerySelect(Selectable.primaryAddress, Selectable.street, Selectable.town, Selectable.postcode, Selectable.transactionDate,
				Selectable.pricePaid);

		String query = buildQuery(select, values);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) objectMapper.readTree(
                queryResponse.get("result").toString())
                .get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {
            ObjectNode currentNode = (ObjectNode) jsonNode;
			String houseName = null;
			String street = null;
			String town = null;
			String postcode = null;
			Date date = null;
			long pricePaid = -1;

			if (currentNode.get("paon").get("value") != null) {
				houseName = currentNode.get("paon").get("value").asText();
			}

			if (currentNode.get("paon").get("value") != null) {
				houseName = currentNode.get("paon").get("value").asText();
			}

			if (currentNode.get("town").get("value") != null) {
				town = currentNode.get("town").get("value").asText();
			}

			if (currentNode.get("street").get("value") != null) {
				street = currentNode.get("street").get("value").asText();
			}

			if (currentNode.get("postcode").get("value") != null) {
				postcode = currentNode.get("postcode").get("value").asText();
			}

			if (currentNode.get("transactionDate").get("value") != null) {
				date = DATE_FORMAT.parse(currentNode.get("transactionDate").get("value").asText());
			}

			if (currentNode.get("pricePaid").get("value") != null) {
				pricePaid = currentNode.get("pricePaid").get("value").asLong();
			}

			transactionsList.add(
                    new LandRegistryDataWithTransaction(
							houseName, street, town, postcode, date, pricePaid
                    )
            );
        }

        return transactionsList;
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
		String query = getQueryPrefixDeclarations() + "\n" + select.buildQuerySelect() + "\n" + values.buildQueryWhere();


		return query;
	}

	private String getQueryPrefixDeclarations() {
		return "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> \n" + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n"
				+ "prefix sr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/> \n" + "prefix ukhpi: <http://landregistry.data.gov.uk/def/ukhpi/> \n"
				+ "prefix lrppi: <http://landregistry.data.gov.uk/def/ppi/> \n" + "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n"
				+ "prefix lrcommon: <http://landregistry.data.gov.uk/def/common/>";
	}

}
