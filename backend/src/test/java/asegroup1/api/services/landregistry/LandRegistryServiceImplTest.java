package asegroup1.api.services.landregistry;

import asegroup1.api.models.heatmap.HeatMapDataPoint;
import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

class LandRegistryServiceImplTest {

    private static LandRegistryServiceImpl landRegistryService;

    private static final long RANDOM_SEED = 8312595207343625996L;

    private static final String[] validPostCodeEnds = {"4AA", "4BL", "1RQ", "1AN"};

    @BeforeAll
    private static void setUpService() {
        landRegistryService = new LandRegistryServiceImpl(null);
    }

    @Test
    void testIfSearchAddressesByPostCodeReturnsCorrectStreet() {
        try {
            //This postcode only has one street name
            List<LandRegistryData> addressByPostCode = landRegistryService.getAddressesForPostCode("BH9 2SL");

            //Checking not only if results are returned but that results contain correct data
            if (!addressByPostCode.get(0).getConstraint(Selectable.street).equals("ENSBURY PARK ROAD")) {
                System.err.println("incorrect street name returned");
                assert false;
            }

            assert true;
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfPassingInvalidPostCodeToAddressSearchReturnsNoAddresses() {
        try {
            assert landRegistryService.getAddressesForPostCode("0").isEmpty();
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfSearchTransactionsByPostCodeReturnsValidPrices() {
        try {
            LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
            constraint.getEqualityConstraints().setPostCode("BN14 7BH");

            List<LandRegistryData> addressByPostCode = landRegistryService.getTransactions(constraint, true);

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
    void testIfPassingInvalidPostCodeToTransactionSearchReturnsNoAddresses() {
        try {
            //Provides the invalid postcode of "0"
            LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
            constraint.getEqualityConstraints().setPostCode("0");

            fail("Constraint should throw an InvalidParameterException");
        } catch (InvalidParameterException e) {
            assert true;
        }
    }

    @Test
    void testIfLatitudeForAddressesAreFetchedAndRoughlyAccurate() {
        List<LandRegistryData> addresses = new LinkedList<>();

        LandRegistryData data = new LandRegistryData();
        data.setPrimaryHouseName("85");
        data.setStreetName("QUEEN STREET");
        data.setTownName("WORTHING");
        data.setPostCode("BN14 7BH");
        addresses.add(data);

        LandRegistryData address = landRegistryService.getPositionForAddresses(addresses).get(0);

        //lat 50.824190 for address
        assert (address.getLatitude() > 50.824 && address.getLatitude() < 50.825);
    }

    @Test
    void testIfLongitudeForAddressesAreFetchedAndRoughlyAccurate() {
        List<LandRegistryData> addresses = new LinkedList<>();

        LandRegistryData data = new LandRegistryData();
        data.setPrimaryHouseName("85");
        data.setStreetName("QUEEN STREET");
        data.setTownName("WORTHING");
        data.setPostCode("BN14 7BH");
        addresses.add(data);

        LandRegistryData address = landRegistryService.getPositionForAddresses(addresses).get(0);

        //long -0.378000 for address
        assert (address.getLongitude() > -0.37900 && address.getLongitude() < -0.37800);
    }

    @Test
    void testIfPositionsVaryBetweenAddressesInStreet() {
        try {
            List<LandRegistryData> addressesForPostCode = landRegistryService.getAddressesForPostCode("BH9 2SL");

            Map<Double, Double> alreadyAccessedCoordinates = new HashMap<>();

            for (LandRegistryData landRegistryData : addressesForPostCode) {
                if (alreadyAccessedCoordinates.get(landRegistryData.getLatitude()) != null)
                    if (alreadyAccessedCoordinates.get(landRegistryData.getLatitude()).equals(landRegistryData.getLongitude())) {
                        System.err.println("Duplicate addresses coordinates found in street in which no duplicate addresses should appear");
                        assert false;
                    }

                alreadyAccessedCoordinates.put(landRegistryData.getLatitude(), landRegistryData.getLongitude());
            }

            assert true;
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfPositionOfAddressesForFlatsAreFetched() {
        try {
            List<LandRegistryData> addressesForPostCode = landRegistryService.getAddressesForPostCode("BN20 7LH");

            for (LandRegistryData landRegistryData : addressesForPostCode) {
                assert landRegistryData.getLatitude() != null &&
                        landRegistryData.getLongitude() != null;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfInvalidPostCodeFetchesNullCoordinates() {
        List<LandRegistryData> addresses = new LinkedList<>();

        //Invalid address
        addresses.add(new LandRegistryData());

        LandRegistryData address = landRegistryService.getPositionForAddresses(addresses).get(0);

        assert address != null;
        assert address.getLatitude() == null && address.getLongitude() == null;
    }

    @Test
    void testIfPriceValuesAreNormalisedCorrectly() {
        List<LandRegistryData> landRegistryDataList = new ArrayList<>();

        for (int i = 0; i < 3; ) {
            LandRegistryData landRegistryData = new LandRegistryData();

            landRegistryData.setPricePaid(++i * 5L);
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
        List<HeatMapDataPoint> heatMapDataPoints = getHeatMapTestData(5L, 10L, 15L);

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

    @Test
    void testIfLargeAddressListIsAggregatedCorrectly() {
        Random random = new Random(RANDOM_SEED);

        List<LandRegistryData> postCodeLocationData = new ArrayList<>();

        for (int i = 0; i < validPostCodeEnds.length; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();
            landRegistryData.setPostCode("BN11 " + validPostCodeEnds[i]);
            landRegistryData.setLatitude(random.nextDouble());
            landRegistryData.setLatitude(random.nextDouble());

            postCodeLocationData.add(landRegistryData);
        }

        JSONObject mockRequest = new JSONObject();

        try {
            mockRequest.put("top", 50.814);
            mockRequest.put("right", -0.376);
            mockRequest.put("bottom", 50.8135);
            mockRequest.put("left", -0.378);


            LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);
            when(landRegistryDataDaoMock.searchForPostCodesInBoundaries(
                    mockRequest.getDouble("top"),
                    mockRequest.getDouble("right"),
                    mockRequest.getDouble("bottom"),
                    mockRequest.getDouble("left")
            )).thenReturn(Arrays.asList("BN11 4AA", "'BN11 4BL", "BN11 1RQ", "BN11 1AN"));

            when(landRegistryDataDaoMock.getLandRegistryDataByPostcode(
                    "WHERE postcode = 'BN11 4AA' OR \n" +
                            "\t postcode = ''BN11 4BL' OR \n" +
                            "\t postcode = 'BN11 1RQ' OR \n" +
                            "\t postcode = 'BN11 1AN'"
            )).thenReturn(postCodeLocationData);

            landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);

            assert landRegistryService.getPositionInsideBounds(mockRequest).size() == 5;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
}