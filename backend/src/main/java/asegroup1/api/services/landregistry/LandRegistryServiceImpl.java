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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

//Does not need to extend ServiceImpl as does not use a Dao
@Service
public class LandRegistryServiceImpl {

	private final LandRegistryDaoImpl postCodeCoordinatesDao;

	@Autowired
	public LandRegistryServiceImpl(LandRegistryDaoImpl postCodeCoordinatesDao) {
		this.postCodeCoordinatesDao = postCodeCoordinatesDao;
	}

	@SuppressWarnings("SpellCheckingInspection")
	private static final String LAND_REGISTRY_SPARQL_ENDPOINT =
		"http://landregistry.data.gov.uk/app/root/qonsole/query";
	private static final String GOOGLE_MAPS_URL =
		"https://maps.googleapis.com/maps/api/geocode/json?address=";

	@Value("${google.maps.api-key}") // Get Api Key from application.properties
	private String googleMapsApiKey;

	//OTHER CONSTANTS
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	// TODO: As this is accessed from other classes move this to a configuration file value
	static final int[] AGGREGATION_LEVELS = new int[]{0, 15, 500};

	private List<LandRegistryData> getTransactions(LandRegistryQuery query) throws IOException, UnirestException {
		List<LandRegistryData> transactionsList = new LinkedList<>();

		String queryStr = query.buildQuery();

		JSONObject queryResponse = executeSPARQLQuery(queryStr);

		ArrayNode transactionListResponse =
			(ArrayNode) OBJECT_MAPPER
				.readTree(queryResponse.get("result").toString())
				.get("results")
				.get("bindings");

		for (JsonNode jsonNode : transactionListResponse) {
			transactionsList.add(new LandRegistryData(jsonNode));
		}

		return transactionsList;
	}

	@SuppressWarnings("unused")
	private List<LandRegistryData> getTransactions(
		LandRegistryQuerySelect select,
		LandRegistryQueryConstraint constraint,
		LandRegistryQueryGroup group
	) throws IOException, UnirestException {
		return getTransactions(new LandRegistryQuery(constraint, group, select));
	}

	List<LandRegistryData> getTransactions(LandRegistryQuerySelect select, LandRegistryQueryConstraint constraint)
		throws IOException, UnirestException {
		return getTransactions(new LandRegistryQuery(constraint, null, select));
	}

	private List<LandRegistryData> getLatestTransactions(
		List<Selectable> values,
		LandRegistryQueryConstraint constraint
	) throws IOException, UnirestException {
		return getTransactions(LandRegistryQuery.buildQueryLatestSalesOnly(constraint, values));
	}

	@SuppressWarnings("unused")
	public List<LandRegistryData> getLatestTransactions(LandRegistryQueryConstraint constraint)
	throws IOException, UnirestException {
		return getLatestTransactions(new ArrayList<>(), constraint);
	}

	private List<LandRegistryData> fetchPostCodesInsideCoordinateBox(
		double top, double right, double bottom, double left
	) {
		return postCodeCoordinatesDao.searchForLandRegistryDataInBoundaries(top, right, bottom, left, true);
	}

	public List<?> getPositionInsideBounds(JSONObject mapPosition)throws UnirestException, IOException {

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
			return addColoursToLandRegistryData(landRegistryDataForPostcodes);
		} else if (postcodesContained > AGGREGATION_LEVELS[0]) {
			LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
			constraint.setMinDate(LocalDate.now().minusYears(LandRegistryData.YEARS_TO_FETCH));

			List<String> postcodes = new ArrayList<>();

			for (LandRegistryData landRegistryDataForPostcode : landRegistryDataForPostcodes) {
				postcodes.add(landRegistryDataForPostcode.getConstraint(Selectable.postcode));
			}

			constraint.setEqualityConstraint(Selectable.postcode, postcodes.toArray(new String[0]));

			return addColoursToLandRegistryData(getPositionForAddresses(getTransactions(
				LandRegistryQuery.buildQueryLatestSalesOnly(
					constraint, Arrays.asList(Selectable.paon, Selectable.street, Selectable.town, Selectable.pricePaid)
			))));
		} else {
			return new ArrayList<>();
		}
	}

	private List<LandRegistryData> addColoursToLandRegistryData(List<LandRegistryData> landRegistryDataForPostcodes) {
		landRegistryDataForPostcodes = landRegistryDataForPostcodes.stream().filter(
			entry -> entry != null
				&& entry.getConstraint(Selectable.pricePaid) != null
				&& entry.getConstraint(Selectable.pricePaid).matches("[0-9]+")
		).collect(Collectors.toList());

		List<Double> numbers = MathUtil.normaliseList(landRegistryDataForPostcodes.stream().map(
			entry -> Double.parseDouble(entry.getConstraint(Selectable.pricePaid))
		).collect(Collectors.toList()));

		for (int i = 0; i < numbers.size(); i++) {
			landRegistryDataForPostcodes.get(i).setColour(getColoursForNormalisedValues(numbers.get(i)));
		}
		return landRegistryDataForPostcodes;
	}

	private List<LandRegistryData> getPositionForAddresses(List<LandRegistryData> addresses) {
		if (addresses.size() >= 100) {
			throw new InvalidParameterException("This method should never be passed more than 100 addresses");
		}

		StringBuilder addressUriBuilder = new StringBuilder();

		for (LandRegistryData address : addresses) {
			addressUriBuilder.append(GOOGLE_MAPS_URL)
				.append(address.getConstraintNotNull(Selectable.paon).replace(" ", "+"))
				.append("+")
				.append(address.getConstraintNotNull(Selectable.street).replace(" ", "+"))
				.append("+")
				.append(address.getConstraintNotNull(Selectable.town).replace(" ", "+"))
				.append("&key=")
				.append(googleMapsApiKey);

			try {
				JSONObject response = postCodeCoordinatesDao.getGeoLocationData(addressUriBuilder.toString());
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

	@SuppressWarnings("SpellCheckingInspection")
	private JSONObject executeSPARQLQuery(String query) throws UnirestException {
		return Unirest.post(LAND_REGISTRY_SPARQL_ENDPOINT)
			.field("output", "json")
			.field("q", query)
			.field("url", "/landregistry/query")
			.asJson()
			.getBody()
			.getObject();
	}

	List<HeatMapDataPoint> convertLandRegistryDataListToHeatMapList(List<LandRegistryData> landRegistryDataList) {
		if (landRegistryDataList.isEmpty()) {
			return new ArrayList<>();
		}
		landRegistryDataList = landRegistryDataList.stream().filter(
			entry -> entry != null
				&& entry.getConstraint(Selectable.pricePaid).matches("[-0-9]+")
		).collect(Collectors.toList());

		List<Double> numbers = MathUtil.normaliseList(landRegistryDataList.stream().map(
			entry -> Double.parseDouble(entry.getConstraint(Selectable.pricePaid))).collect(Collectors.toList())
		);

		List<HeatMapDataPoint> heatMapDataPoints = new ArrayList<>();

		for (int i = 0; i < landRegistryDataList.size(); i++) {
			LandRegistryData lr = landRegistryDataList.get(i);
			heatMapDataPoints.add(new HeatMapDataPoint(
				lr.getLatitude(),
				lr.getLongitude(),
				getColoursForNormalisedValues(numbers.get(i)),
				lr.getRadius()
			));
		}

		return heatMapDataPoints;
	}

	private Colour getColoursForNormalisedValues(Double normalisedValue) {
		// The higher the normalised value the darker the red will appear
		return new Colour((55 + (int) (normalisedValue * 200)));
	}

	private HashMap<String, Long> getAllPostcodePrices(String... postcodes) throws IOException, UnirestException {
		List<LandRegistryData> transactions = getTransactions(
			LandRegistryQuery.buildQueryAveragePricePostcode(postcodes)
		);
		HashMap<String, Long> postcodePrices = new HashMap<>();

		for (LandRegistryData data : transactions) {
			String postcode = data.getConstraint(Selectable.postcode);
			String priceStr = data.getConstraint(Selectable.pricePaid);

			if (postcode != null && priceStr != null) {
				try {
					Long pricePaid = Long.parseLong(priceStr);
					postcodePrices.put(postcode, pricePaid);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		// Map all postcodes without a average price to null
		List<String> unmatchedPostcodes = new ArrayList<>(Arrays.asList(postcodes));
		unmatchedPostcodes.removeAll(postcodePrices.keySet());

		for (String postcode : unmatchedPostcodes) {
			postcodePrices.put(postcode, null);
		}

		return postcodePrices;
	}

	public void updatePostcodeDatabase(String postcodePrefix) throws IOException, UnirestException {
		long startTime = System.currentTimeMillis();
		int updatedRecords = 0;

		HashMap<String, List<String>> postcodeAreas =
			postCodeCoordinatesDao.getMatchingPostcodes(postcodePrefix, false, 1);
		double numAreas = postcodeAreas.size();
		double numDone = 0;

		for (Entry<String, List<String>> postcodeArea : postcodeAreas.entrySet()) {
			long estTimeLeft = numDone == 0 // This accounts for if number done is 0 otherwise 0 division is possible
				? Long.MAX_VALUE
				: Math.round(((System.currentTimeMillis() - startTime) / numDone) * (numAreas - numDone)) / 1000;

			System.out.printf("Updating records in %-9s %.3f %% done, %01dH %02dM %02dS remaining\n",
				"\"" + postcodeArea.getKey() + "\"",
				(numDone / numAreas) * 100,
				estTimeLeft / 3600,
				(estTimeLeft % 3600) / 60,
				(estTimeLeft % 60)
			);
			List<String> postcodes = postcodeArea.getValue();
			HashMap<String, Long> newPrices = getAllPostcodePrices(postcodes.toArray(new String[0]));
			updatedRecords += postCodeCoordinatesDao.updateAveragePrice(newPrices);
			numDone++;
		}

		System.out.println("Updated " + updatedRecords + " records in " + (System.currentTimeMillis() - startTime) + "ms");
		System.out.println("Done in " + (System.currentTimeMillis() - startTime) + "ms.");
	}
}
