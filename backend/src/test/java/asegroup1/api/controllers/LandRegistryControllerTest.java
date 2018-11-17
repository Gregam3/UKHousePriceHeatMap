package asegroup1.api.controllers;


import asegroup1.api.services.landregistry.LandRegistryServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
class LandRegistryControllerTest {

    private static LandRegistryController landRegistryController;

    @BeforeAll
    public static void setUp() {
        landRegistryController = new LandRegistryController(
                new LandRegistryServiceImpl(
                        null
                )
        );
    }

    @Test
    void testIfNonSpacedPostCodeIsFormattedIntoQueryableFormat() {
        ResponseEntity<?> response = landRegistryController.getAddressDataForPostCode("BH92SL");

        assert !((List) response.getBody()).isEmpty();
    }

    @Test
    void testIfLowercasePostCodeIsFormattedIntoQueryableFormat() {
        ResponseEntity<?> response = landRegistryController.getAddressDataForPostCode("bh9 2sl");

        assert !((List) response.getBody()).isEmpty();
    }

}