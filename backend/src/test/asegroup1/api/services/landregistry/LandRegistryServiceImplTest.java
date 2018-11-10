package asegroup1.api.services.landregistry;

import asegroup1.api.models.Address;
import asegroup1.api.models.AddressWithTransaction;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
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
            landRegistryService = new LandRegistryServiceImpl();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testIfSearchAddressesByPostCodeReturnsCorrectStreet() {
        try {
            //This postcode only has one street name
            List<Address> addressByPostCode = landRegistryService.getAddressesByPostCode("BH9 2SL");

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
            assert landRegistryService.getAddressesByPostCode("0").isEmpty();
        } catch (IOException | UnirestException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testIfSearchTransactionsByPostCodeReturnsValidPrices() {
        try {
            List<AddressWithTransaction> addressByPostCode = landRegistryService.getTransactionsByPostCode("BN14 7BH");

            //Checking not only if results are returned but that results contain valid data
            if (addressByPostCode.get(0).getPrice() <= 0) {
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
            assert landRegistryService.getTransactionsByPostCode("0").isEmpty();
        } catch (IOException | UnirestException | ParseException e) {
            e.printStackTrace();
            assert false;
        }
    }
}