package asegroup1.api.services.landregistry;

import asegroup1.api.models.Address;
import asegroup1.api.models.AddressWithTransaction;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

class LandRegistryServiceImplTest {

    private static LandRegistryServiceImpl landRegistryService;

    @BeforeAll
    private static void setUpService() {
        try {
            landRegistryService = new LandRegistryServiceImpl(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testIfSearchAddressesByPostCodeReturnsCorrectStreet() {
        try {
            //This postcode only has one street name
            List<Address> addressByPostCode = landRegistryService.getAddressesForPostCode("BH9 2SL");

            //Checking not only if results are returned but that results contain correct data
            if (!addressByPostCode.get(0).getStreetName().equals("ENSBURY PARK ROAD")) {
                System.err.println("incorrect street name returned");
                assert false;
            }

            assert true;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfPassingInvalidPostCodeToAddressSearchReturnsNoAddresses() {
        try {
            assert landRegistryService.getAddressesForPostCode("0").isEmpty();
        } catch (IOException | UnirestException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfSearchTransactionsByPostCodeReturnsValidPrices() {
        try {
            List<AddressWithTransaction> addressByPostCode = landRegistryService.getTransactionsForPostCode("BN14 7BH");

            //Checking not only if results are returned but that results contain valid data
            if ((addressByPostCode.get(0)).getPrice() <= 0) {
                System.err.println("Transaction has invalid price");
                assert false;
            }

            assert true;
        } catch (IOException | UnirestException | ParseException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfPassingInvalidPostCodeToTransactionSearchReturnsNoAddresses() {
        try {
            //Provides the invalid postcode of "0"
            assert landRegistryService.getTransactionsForPostCode("0").isEmpty();
        } catch (IOException | UnirestException | ParseException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfLatitudeForAddressesAreFetchedAndRoughlyAccurate() {
        List<Address> addresses = new LinkedList<>();

        addresses.add(new Address(
                "85",
                "QUEEN STREET",
                "WORTHING",
                "BN14 7BH"
        ));

        Address address = landRegistryService.getPositionForAddresses(addresses).get(0);

        //lat 50.824190 for address
        assert (address.getLatitude() > 50.822 && address.getLatitude() < 50.824);
    }

    @Test
    void testIfLongitudeForAddressesAreFetchedAndRoughlyAccurate() {
        List<Address> addresses = new LinkedList<>();

        addresses.add(new Address(
                "85",
                "QUEEN STREET",
                "WORTHING",
                "BN14 7BH"
        ));

        Address address = landRegistryService.getPositionForAddresses(addresses).get(0);

        //long -0.378000 for address
        assert (address.getLongitude() > -0.37700 && address.getLongitude() < -0.37500);
    }

    @Test
    void testIfInvalidPostCodeFetchesNullCoordinates() {
        List<Address> addresses = new LinkedList<>();

        //Invalid address
        addresses.add(new Address(
                "",
                "",
                "",
                ""
        ));

        Address address = landRegistryService.getPositionForAddresses(addresses).get(0);

        assert address != null;
        assert address.getLatitude() == null && address.getLongitude() == null;
    }
}