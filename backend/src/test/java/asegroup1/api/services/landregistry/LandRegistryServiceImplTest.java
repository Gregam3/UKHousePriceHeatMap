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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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

    @Test
    void testIfLongitudeForAddressesAreFetchedAndRoughlyAccurate() {
        List<LandRegistryData> addresses = new LinkedList<>();

        LandRegistryData data = new LandRegistryData();
        data.setPrimaryHouseName("85");
        data.setStreetName("QUEEN STREET");
        data.setTownName("WORTHING");
        data.setPostCode("XXX XXXX");
        addresses.add(data);

        LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

        JSONObject response = new JSONObject();
        JSONObject mockRequest = new JSONObject();
        try {

            response.put("lat", 0);
            response.put("lng", 0);


            mockRequest.put("top", 50.814);
            mockRequest.put("right", -0.376);
            mockRequest.put("bottom", 50.8135);
            mockRequest.put("left", -0.378);

            when(landRegistryDataDaoMock.getGeoLocationData("https://maps.googleapis.com/maps/api/geocode/json?address=++&key=AIzaSyBGmy-uAlzvXRLcQ_krAaY0idR1KUTJRmA")).thenReturn(response);
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

    @Test
    void testIfPriceValuesAreNormalisedCorrectly() {
        List<LandRegistryData> landRegistryDataList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();

            landRegistryData.setPricePaid(i * 5L);
            landRegistryData.setLongitude(0);
            landRegistryData.setLatitude(0);
            landRegistryDataList.add(landRegistryData);
        }

        List<HeatMapDataPoint> heatMapDataPoints = landRegistryService.convertLandRegistryDataListToHeatMapList(landRegistryDataList);

        assert heatMapDataPoints.get(0).getColour().getRed() == 255 &&
                heatMapDataPoints.get(1).getColour().getRed() == 155 &&
                heatMapDataPoints.get(2).getColour().getRed() == 55;

    }

    private List<HeatMapDataPoint> getHeatMapTestData(long... values) {
        List<LandRegistryData> landRegistryDataList = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();

            landRegistryData.setPricePaid(values[i]);
            landRegistryData.setLongitude(0);
            landRegistryData.setLatitude(0);


            landRegistryDataList.add(landRegistryData);
        }

        return landRegistryService.convertLandRegistryDataListToHeatMapList(landRegistryDataList);
    }

    @Test
    void testIfNormalisedValuesConvertToCorrectColours() {
        List<HeatMapDataPoint> heatMapDataPoints = getHeatMapTestData(5L, 10L, 15L);

        //Check if 15 converted to red is darker red than 10 converted to red, and then check if 10 converted to red is darker red than 5 converted to red
        assert heatMapDataPoints.get(0).getColour().getRed() > heatMapDataPoints.get(1).getColour().getRed()
                && heatMapDataPoints.get(1).getColour().getRed() > heatMapDataPoints.get(2).getColour().getRed();
    }

    @Test
    void testHowNormaliseValuesReturns0ValueForOnlyOneDistinctValue() {
        List<HeatMapDataPoint> heatMapDataPoints = getHeatMapTestData(5L, 5L, 5L);

        for (HeatMapDataPoint heatMapDataPoint : heatMapDataPoints) {
            assert heatMapDataPoint.getColour().getRed() == 255;
        }
    }

    @Test
    void testHowNormaliseValuesHandlesEmptyList() {
        assert landRegistryService.convertLandRegistryDataListToHeatMapList(new ArrayList<>()) == null;
    }

    @Test
    void testIfNormalisedValuesConvertToCorrectColoursWithNegativeValues() {
        List<HeatMapDataPoint> heatMapDataPoints = getHeatMapTestData(-5L, -15L, -10L);

        //Check if 15 converted to red is darker red than 10 converted to red, and then check if 10 converted to red is darker red than 5 converted to red
        assert heatMapDataPoints.get(0).getColour().getRed() < heatMapDataPoints.get(2).getColour().getRed()
                && heatMapDataPoints.get(2).getColour().getRed() < heatMapDataPoints.get(1).getColour().getRed();

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
}