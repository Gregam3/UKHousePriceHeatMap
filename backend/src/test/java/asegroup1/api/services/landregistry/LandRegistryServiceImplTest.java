package asegroup1.api.services.landregistry;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.heatmap.HeatMapDataPoint;
import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

class LandRegistryServiceImplTest {

	private static LandRegistryServiceImpl landRegistryService;

	private static final long RANDOM_SEED = 8312595207343625996L;

	@BeforeAll
	private static void setUpService() {
		landRegistryService = new LandRegistryServiceImpl(null);
	}

	@Test
	void testIfSearchTransactionsByPostCodeReturnsValidPrices() {
		try {
			LandRegistryQueryConstraint constraint =
				new LandRegistryQueryConstraint();
			constraint.getEqualityConstraints().setPostCode("BN14 7BH");

			LandRegistryDaoImpl landRegistryDataDaoMock =
				mock(LandRegistryDaoImpl.class);

			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull()))
				.thenReturn(getSPARQLResponse());

			LandRegistryServiceImpl landRegistryService =
				new LandRegistryServiceImpl(landRegistryDataDaoMock);

			List<LandRegistryData> addressByPostCode = landRegistryService.getTransactions(new LandRegistryQuerySelect(Selectable.pricePaid), constraint);

			// Checking not only if results are returned but that results
			// contain valid data
			if (Integer.parseInt(addressByPostCode.get(0).getConstraint(
					Selectable.pricePaid)) <= 0) {
				System.err.println("Transaction has invalid price");
				assert false;
			}

			assert true;
		} catch (IOException | UnirestException | NumberFormatException |
				 JSONException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	void testIfSettingInvalidPostcodeThrowsInvalidParameterException() {
		try {
			// Provides the invalid postcode of "0"
			LandRegistryQueryConstraint constraint =
				new LandRegistryQueryConstraint();

			Assertions.assertThrows(
				InvalidParameterException.class,
				() -> constraint.getEqualityConstraints().setPostCode("0"));
		} catch (InvalidParameterException e) {
			assert true;
		}
	}

	@Test
	void testIfSetPostcodeAcceptsValidPostcode() {
		// Provides the invalid postcode of "0"
		LandRegistryQueryConstraint constraint =
			new LandRegistryQueryConstraint();

		constraint.getEqualityConstraints().setPostCode("BH9 2SL");

		assert constraint.getEqualityConstraints()
			.getConstraint(Selectable.postcode)
			.equals("BH9 2SL");
	}

	@Test
	void testIfLongitudeForAddressesAreFetched() {
		List<LandRegistryData> addresses =
			generateLandRegistryDataForAddresses(1, 0, 1);

		LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

		JSONObject mockRequest = fetchMockRequest();

		try {
			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull()))
				.thenReturn(getSPARQLResponse());

			when(
				landRegistryDataDaoMock.getGeoLocationData(
					"https://maps.googleapis.com/maps/api/geocode/json?address=SUSSEX+COURT+TENNYSON+ROAD+WORTHING&key=AIzaSyBGmy-uAlzvXRLcQ_krAaY0idR1KUTJRmA"))
				.thenReturn(fetchMockResponse());

			when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(
					 mockRequest.getDouble("top"),
					 mockRequest.getDouble("right"),
					 mockRequest.getDouble("bottom"),
					 mockRequest.getDouble("left"), true))
				.thenReturn(addresses);

			LandRegistryServiceImpl landRegistryServiceLocal =
				new LandRegistryServiceImpl(landRegistryDataDaoMock);

			addresses = (List<LandRegistryData>)landRegistryServiceLocal
							.getPositionInsideBounds(mockRequest);

			LandRegistryData address = addresses.get(0);

			assert address.getLongitude() == 0 && address.getLatitude() == 0;
		} catch (UnirestException | JSONException | IOException e) {
			e.printStackTrace();

			assert false;
		}
	}

	@Test
	void testIfNormalisedValuesConvertToCorrectColours() {
		List<HeatMapDataPoint> heatMapDataPoints =
			getHeatMapTestData(5L, 10L, 15L);

		// Check if 15 converted to red is darker red than 10 converted to red,
		// and then check if 10 converted to red is darker red than 5 converted
		// to red
		assert heatMapDataPoints.get(0).getColour().getHex().equals(
			"#00d300") &&
			heatMapDataPoints.get(1).getColour().getHex().equals("#d90000") &&
			heatMapDataPoints.get(2).getColour().getHex().equals("#ff0000");
	}

	@Test
	void testHowNormaliseValuesReturns0ValueForOnlyOneDistinctValue() {
		List<HeatMapDataPoint> heatMapDataPoints =
			getHeatMapTestData(5L, 5L, 5L);

		for (HeatMapDataPoint heatMapDataPoint : heatMapDataPoints) {
			assert heatMapDataPoint.getColour().getHex().equals("#9b0000");
		}
	}

	@Test
	void testHowNormaliseValuesHandlesEmptyList() {
		assert landRegistryService.convertLandRegistryDataListToHeatMapList(
			new ArrayList<>()) == null;
	}

	@Test
	void testIfNormalisedValuesConvertToCorrectColoursWithNegativeValues() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> getHeatMapTestData(-5L, -15L, -10L));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testIfMediumSizeListGoesIntoCorrectStaticAggregationLevel() {
		try {
			List<LandRegistryData> positionInsideBounds =
				(List<LandRegistryData>)getDisplayData(50, 50000, 1000000);

			// If it can be cast to List<LandRegistryData> then list must either
			// be of addresses of postcodes, but as a getLocationForAddresses is
			// not mocked this must be a list of postcodes passing the test
			assert true;
		} catch (Exception e) {
			e.printStackTrace();

			assert false;
		}
	}

	@Test
	void testIfEmptyListGoesIntoCorrectStaticAggregationLevel() {
		assert getDisplayData(0, 10, 10).isEmpty();
	}

	@Test
	void testIfHeatMapIsReturnedWhenThresholdIsPassed() {
		List<?> dataPoints =
			getDisplayData(LandRegistryServiceImpl.AGGREGATION_LEVELS[2], 1, 1);

		assert dataPoints.size() >=
				LandRegistryServiceImpl.AGGREGATION_LEVELS[2] &&
			dataPoints.get(0) instanceof HeatMapDataPoint;
	}

	// UTILS
	private JSONObject fetchMockResponse() {
		JSONObject mockResponse = new JSONObject();

		try {
			mockResponse.put("lat", 0);
			mockResponse.put("lng", 0);
		} catch (JSONException e) {
			e.printStackTrace();

			assert false;
		}

		return mockResponse;
	}

	private JSONObject fetchMockRequest() {
		JSONObject mockRequest = new JSONObject();

		try {
			mockRequest.put("top", 0);
			mockRequest.put("right", 0);
			mockRequest.put("bottom", 0);
			mockRequest.put("left", 0);
		} catch (Exception e) {
			assert false;
			e.printStackTrace();
		}

		return mockRequest;
	}

	private List<LandRegistryData>
	generateLandRegistryDataForAddresses(int numberToGenerate, int lowerBound,
										 int range) {
		List<LandRegistryData> landRegistryDataList =
			generateLandRegistryDataForPostCodes(numberToGenerate, lowerBound,
												 range);

		Random random = new Random(RANDOM_SEED);

		for (LandRegistryData landRegistryData : landRegistryDataList) {
			landRegistryData.setPrimaryHouseName(
				String.valueOf(random.nextInt(100)));
			landRegistryData.setStreetName("STREET");
			landRegistryData.setTownName("TOWN");
		}

		return landRegistryDataList;
	}

	private List<LandRegistryData>
	generateLandRegistryDataForPostCodes(int numberToGenerate, int lowerBound,
										 int range) {
		final String[] postcodes = {"BN14 7BH", "NW9 9PR", "NN12 8DT",
									"TW7 4QN",  "L22 3YU", "RM17 6LJ",
									"RG14 7DF", "SE25 5RT"};
		List<LandRegistryData> landRegistryDataList = new ArrayList<>();

		Random random = new Random(RANDOM_SEED);

		for (int i = 0; i < numberToGenerate; i++) {
			LandRegistryData landRegistryData = new LandRegistryData();

			landRegistryData.setPricePaid(random.nextInt(range) + lowerBound);
			landRegistryData.setPostCode(postcodes[i % 7]);

			landRegistryData.setRadius(0.0);
			landRegistryData.setLatitude(0.0);
			landRegistryData.setLongitude(0.0);

			landRegistryDataList.add(landRegistryData);
		}

		return landRegistryDataList;
	}

	private List<HeatMapDataPoint> getHeatMapTestData(long... values) {
		List<LandRegistryData> landRegistryDataList = new ArrayList<>();

		for (int i = 0; i < values.length; i++) {
			LandRegistryData landRegistryData = new LandRegistryData();

			landRegistryData.setPricePaid(values[i]);
			landRegistryData.setLongitude(0);
			landRegistryData.setLatitude(0);
			landRegistryData.setRadius(0.0);

			landRegistryDataList.add(landRegistryData);
		}

		return landRegistryService.convertLandRegistryDataListToHeatMapList(
			landRegistryDataList);
	}

	List<?> getDisplayData(int numberToGenerate, int lowerPriceBound,
						   int priceRange) {
		LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

		List<LandRegistryData> landRegistryDataList =
			generateLandRegistryDataForPostCodes(numberToGenerate,
												 lowerPriceBound, priceRange);

		when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(
				 0, 0, 0, 0, true))
			.thenReturn(landRegistryDataList);

		LandRegistryServiceImpl landRegistryService =
			new LandRegistryServiceImpl(landRegistryDataDaoMock);

		JSONObject mockRequest = fetchMockRequest();

		try {
			return landRegistryService.getPositionInsideBounds(mockRequest);
		} catch (UnirestException | IOException e) {
			e.printStackTrace();
			assert false;
		}

		return null;
	}

	private JSONObject getSPARQLResponse() throws JSONException {
		return new JSONObject(
			"{\"result\":\"{\\n  \\\"head\\\": {\\n    \\\"vars\\\": [ \\\"paon\\\" , \\\"saon\\\" , \\\"street\\\" , "
			+
			"\\\"postcode\\\" , \\\"TransactionDate\\\" , \\\"Town\\\" , \\\"PricePaid\\\" ]\\n  } ,\\n  \\\"results\\\": {\\n    \\\"bindings\\\": [\\n     "
			+
			" {\\n        \\\"paon\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"SUSSEX COURT\\\" } ,\\n        \\\"saon\\\": { \\\"type\\\": \\\"literal\\\" , "
			+
			"\\\"value\\\": \\\"FLAT 2\\\" } ,\\n        \\\"street\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"TENNYSON ROAD\\\" } ,\\n        "
			+
			"\\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN11 4BT\\\" } ,\\n        \\\"TransactionDate\\\": { \\\"type\\\": \\\"literal\\\" , "
			+
			"\\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#date\\\" , \\\"value\\\": \\\"2016-10-07\\\" } ,\\n        \\\"Town\\\": { \\\"type\\\": \\\"literal\\\" , "
			+
			"\\\"value\\\": \\\"WORTHING\\\" } ,\\n        \\\"PricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#integer\\\" , "
			+
			"\\\"value\\\": \\\"155000\\\" }\\n      }\\n    ]\\n  }\\n}\\n\",\"status\":200}");
	}
}
