package asegroup1.api.services.landregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.heatmap.HeatMapDataPoint;
import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect;

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
            LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
            constraint.getEqualityConstraints().setPostCode("BN14 7BH");


            List<LandRegistryData> addressByPostCode = landRegistryService.getTransactions(new LandRegistryQuerySelect(Selectable.pricePaid), constraint);

            //Checking not only if results are returned but that results contain valid data
            if (Integer.parseInt(addressByPostCode.get(0).getConstraint(Selectable.pricePaid)) <= 0) {
                System.err.println("Transaction has invalid price");
                assert false;
            }

            assert true;
        } catch (IOException | UnirestException | NumberFormatException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfSettingInvalidPostcodeThrowsInvalidParameterException() {
        try {
            //Provides the invalid postcode of "0"
            LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();

            Assertions.assertThrows(InvalidParameterException.class, () -> constraint.getEqualityConstraints().setPostCode("0"));
        } catch (InvalidParameterException e) {
            assert true;
        }
    }

    @Test
    void testIfSetPostcodeAcceptsValidPostcode() {
        //Provides the invalid postcode of "0"
        LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();

        constraint.getEqualityConstraints().setPostCode("BH9 2SL");

        assert constraint.getEqualityConstraints().getConstraint(Selectable.postcode).equals("BH9 2SL");
    }


    //@Test
    //TODO move SPARQL to dao and mock
    void testIfLongitudeForAddressesAreFetchedAndRoughlyAccurate() {
        List<LandRegistryData> addresses = new LinkedList<>();

        LandRegistryData data = new LandRegistryData();
        data.setPrimaryHouseName("85");
        data.setStreetName("QUEEN STREET");
        data.setTownName("WORTHING");
        data.setPricePaid(100);
        data.setPostCode("XXX XXXX");

        addresses.add(data);

        LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

        JSONObject response = new JSONObject();
        JSONObject mockRequest = new JSONObject();
        try {

            response.put("lat", 0);
            response.put("lng", 0);


            mockRequest.put("top", 0);
            mockRequest.put("right", 0);
            mockRequest.put("bottom", 0);
            mockRequest.put("left", 0);

            when(landRegistryDataDaoMock.getGeoLocationData(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=++&key=AIzaSyBGmy-uAlzvXRLcQ_krAaY0idR1KUTJRmA"
            )).thenReturn(response);

            when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(
                    mockRequest.getDouble("top"),
                    mockRequest.getDouble("right"),
                    mockRequest.getDouble("bottom"),
                    mockRequest.getDouble("left"),
                    true
            )).thenReturn(addresses);


            LandRegistryServiceImpl landRegistryServiceLocal = new LandRegistryServiceImpl(landRegistryDataDaoMock);

            addresses = (List<LandRegistryData>) landRegistryServiceLocal.getPositionInsideBounds(mockRequest);

            LandRegistryData address = addresses.get(0);

            assert address.getLongitude() == 0 && address.getLatitude() == 0;
        } catch (UnirestException | JSONException | IOException e) {
            e.printStackTrace();

            assert false;
        }

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

        return landRegistryService.convertLandRegistryDataListToHeatMapList(landRegistryDataList);
    }

    @Test
    void testIfNormalisedValuesConvertToCorrectColours() {
        List<HeatMapDataPoint> heatMapDataPoints = getHeatMapTestData(5L, 10L, 15L);

        //Check if 15 converted to red is darker red than 10 converted to red, and then check if 10 converted to red is darker red than 5 converted to red
		assert heatMapDataPoints.get(0).getColour().getHex().equals(
			"#00d300") &&
			heatMapDataPoints.get(1).getColour().getHex().equals("#d90000") &&
			heatMapDataPoints.get(2).getColour().getHex().equals("#ff0000");
	}

    @Test
    void testHowNormaliseValuesReturns0ValueForOnlyOneDistinctValue() {
        List<HeatMapDataPoint> heatMapDataPoints = getHeatMapTestData(5L, 5L, 5L);

        for (HeatMapDataPoint heatMapDataPoint : heatMapDataPoints) {
            assert heatMapDataPoint.getColour().getHex().equals("#9b0000");
        }
    }

    @Test
    void testHowNormaliseValuesHandlesEmptyList() {
        assert landRegistryService.convertLandRegistryDataListToHeatMapList(new ArrayList<>()) == null;
    }

    @Test
    void testIfNormalisedValuesConvertToCorrectColoursWithNegativeValues() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> getHeatMapTestData(-5L, -15L, -10L));
	}

    @SuppressWarnings("unchecked")
    @Test
    void testIfPostcodesAreAggregatedCorrectly() {
        LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

        String[] postcodes = {"BN14 7BH", "NW9 9PR", "NN12 8DT", "TW7 4QN", "L22 3YU", "RM17 6LJ", "RG14 7DF", "SE25 5RT"};


        List<LandRegistryData> landRegistryDataList = new ArrayList<>();

        for (int i = 0; i < LandRegistryServiceImpl.AGGREGATION_LEVELS[1] + 1; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();
            landRegistryData.setPostCode(postcodes[i % postcodes.length]);

            landRegistryDataList.add(landRegistryData);
        }

        when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(0, 0, 0, 0, true))
                .thenReturn(landRegistryDataList);

        LandRegistryServiceImpl landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);

        JSONObject mockRequest = new JSONObject();

        try {
            mockRequest.put("top", 0);
            mockRequest.put("right", 0);
            mockRequest.put("bottom", 0);
            mockRequest.put("left", 0);
            for (LandRegistryData positionInsideBound : (List<LandRegistryData>) landRegistryService.getPositionInsideBounds(mockRequest)) {
                //If addresses are fetched constraint size will be greater than 1
                assert positionInsideBound.getAllConstraints().size() == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();

            assert false;
        }
    }

    @Test
    void testIfHeatMapIsReturnedWhenThresholdIsPassed() {
        Random random = new Random(RANDOM_SEED);

        LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

        String[] postcodes = {"BN14 7BH", "NW9 9PR", "NN12 8DT", "TW7 4QN", "L22 3YU", "RM17 6LJ", "RG14 7DF", "SE25 5RT"};


        List<LandRegistryData> landRegistryDataList = new ArrayList<>();

        for (int i = 0; i <= LandRegistryServiceImpl.AGGREGATION_LEVELS[2]; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();
            landRegistryData.setPostCode(postcodes[i % postcodes.length]);
            landRegistryData.setLatitude(0);
            landRegistryData.setLongitude(0);
            landRegistryData.setPricePaid(random.nextInt(10000000));
			landRegistryData.setRadius(0.0);
			landRegistryDataList.add(landRegistryData);
        }

        when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(0, 0, 0, 0, true))
                .thenReturn(landRegistryDataList);

        LandRegistryServiceImpl landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);

        JSONObject mockRequest = new JSONObject();

        try {
            mockRequest.put("top", 0);
            mockRequest.put("right", 0);
            mockRequest.put("bottom", 0);
            mockRequest.put("left", 0);
            List<?> positionsInsideBounds = landRegistryService.getPositionInsideBounds(mockRequest);

            assert positionsInsideBounds.size() >= LandRegistryServiceImpl.AGGREGATION_LEVELS[2] && positionsInsideBounds.get(0) instanceof HeatMapDataPoint;
        } catch (Exception e) {
            e.printStackTrace();

            assert false;
        }
    }


	/**
	 * Test method for
	 * {@link asegroup1.api.services.landregistry.LandRegistryServiceImpl#getAllPostcodePrices(String...)}.
	 */
	@Test
	void testGetAllPostcodePrices() {
		String[] postcodes = new String[] { "BN23 7LE", "BN23 7LL", "BN23 7LN", "BN23 7LQ", "BN23 7LX", "BN23 7LZ" };
		try {
			HashMap<String, Long> prices = landRegistryService.getAllPostcodePrices(postcodes);
			assertNotNull(prices);
			assertTrue(prices.size() == 6);
			for (Entry<String, Long> entry : prices.entrySet()) {
				assertNotNull(entry.getKey());
				if(entry.getValue() == null) {
					assertEquals("BN23 7LX", entry.getKey());
				}
			}
		} catch (IOException | UnirestException e) {
			fail(e);
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.services.landregistry.LandRegistryServiceImpl#getTransactions(asegroup1.api.models.landregistry.LandRegistryQuery)}.
	 */
	@Test
	void testGetTransaction() {
		String[] postcodes = new String[] { "BN23 7LE", "BN23 7LL", "BN23 7LN", "BN23 7LQ", "BN23 7LT", "BN23 7LZ" };
		try {
			for (LandRegistryData transaction : landRegistryService.getTransactions(LandRegistryQuery.buildQueryAveragePricePostcode(postcodes))) {
				assertNotNull(transaction);
				assertEquals(2, transaction.getAllConstraints().size());
				assertTrue(transaction.hasConstraint(Selectable.pricePaid));
				assertTrue(transaction.hasConstraint(Selectable.postcode));
				assertNull(transaction.getLatitude());
				assertNull(transaction.getLongitude());
				assertNull(transaction.getRadius());

			}

		} catch (IOException | UnirestException e) {
			fail(e);
		}
	}
	
	/**
	 * Test method for
	 * {@link asegroup1.api.services.landregistry.LandRegistryServiceImpl#getTransactions(asegroup1.api.models.landregistry.LandRegistryQuery)}.
	 */
	@Test
	void testUpdatePostcodeDatabase() {
		
	}
}
