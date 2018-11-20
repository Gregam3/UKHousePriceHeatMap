package asegroup1.api.services.landregistry;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

class LandRegistryServiceImplTest {

    private static LandRegistryServiceImpl landRegistryService;

    private static final long RANDOM_SEED = 8312595207343625996L;

    private static final String[] validPostCodeEnds = {"9PH", "9PJ", "9PL", "9PN"};

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
    void testIfLargeAddressListIsAggregatedCorrectly() {
        Random random = new Random(RANDOM_SEED);

        List<LandRegistryData> postCodeLocationData = new ArrayList<>();

        for (int i = 0; i < validPostCodeEnds.length; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();
            landRegistryData.setPostCode("BN14 " + validPostCodeEnds[i % 4]);
            landRegistryData.setLatitude(random.nextDouble());
            landRegistryData.setLatitude(random.nextDouble());

            postCodeLocationData.add(landRegistryData);
        }

        LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);
        when(landRegistryDataDaoMock.getLandRegistryDataByPostcode(
                "WHERE postcode = 'BN14 9PH' OR \n" +
                        "\t postcode = 'BN14 9PJ' OR \n" +
                        "\t postcode = 'BN14 9PL' OR \n" +
                        "\t postcode = 'BN14 9PN'"
        )).thenReturn(postCodeLocationData);

        landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);

        List<LandRegistryData> addresses = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            LandRegistryData landRegistryData = new LandRegistryData();
            landRegistryData.setPostCode("BN14 " + validPostCodeEnds[i % 4]);

            addresses.add(landRegistryData);
        }

        assert landRegistryService.getPositionForLocations(addresses).size() == 4;
    }
}