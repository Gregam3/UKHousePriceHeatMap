package asegroup1.api.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.services.landregistry.LandRegistryServiceImpl;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@RestController
@RequestMapping("/land-registry/")
public class LandRegistryController {

    private Properties mockResponses;
    private LandRegistryServiceImpl landRegistryService;

    @Autowired
    public LandRegistryController(LandRegistryServiceImpl landRegistryService) {
        try {
            FileInputStream fakeResponsesInputStream = new FileInputStream(new File("src/main/java/asegroup1/api/controllers/fake-responses.properties"));
            mockResponses = new Properties();
            mockResponses.load(fakeResponsesInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.landRegistryService = landRegistryService;
    }

    @GetMapping("get-addresses/{post-code}")
    public ResponseEntity<?> getAddressDataForPostCode(@PathVariable("post-code") String postCode) {
        try {
            return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getAddressesForPostCode(formatPostCode(postCode))), HttpStatus.OK);
        } catch (UnirestException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("get-display-data")
    public ResponseEntity<?> getDataToDisplayOnMap(@RequestParam JSONObject mapPosition) {
        long timer = System.currentTimeMillis();

        try {
            for (String jsonKey : new String[]{"top", "bottom", "left", "right"}) {
                if (mapPosition.isNull(jsonKey))
                    throw new InvalidParameterException("Value \"" + jsonKey + "\" could not be found, please ensure requestbody contains this value as a top level node");
            }

            List<?> positionsInsideBounds = landRegistryService.getPositionInsideBounds(mapPosition);

            System.out.println(
                    "\n-----------------------------------------------------------------------------------------------------\n" +
                            "\t\t\t\t\t\t\tRequest took " + (System.currentTimeMillis() - timer) + "ms to fetch " + positionsInsideBounds.size() + " elements \n " +
                            "-----------------------------------------------------------------------------------------------------"
            );

            return new ResponseEntity<>(positionsInsideBounds, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred whilst handling this request: " + e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "get-display-data-test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTestDisplayData() {
        return new ResponseEntity<>(mockResponses.getProperty("addressData"), HttpStatus.OK);
    }

    @GetMapping("get-transactions/{post-code}")
    public ResponseEntity<?> getTransactionDataForPostCode(@PathVariable("post-code") String postCode) {
        LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
        constraint.setEqualityConstraint(Selectable.postcode, formatPostCode(postCode));
        constraint.setMinDate(LocalDate.now().minusYears(LandRegistryData.YEARS_TO_FETCH));

        try {
            return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getLatestTransactions(new ArrayList<>(EnumSet.allOf(Selectable.class)), constraint)),
                    HttpStatus.OK);
        } catch (IOException | UnirestException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("update-postcode/{prefix}")
    public ResponseEntity<?> updateTransactionData(@PathVariable("prefix") String prefix) {
        if (prefix == null) {
            prefix = "";
        } else if (!prefix.matches("[\\p{Alnum} ]+")) {
            return new ResponseEntity<>("Invalid postcode pattern", HttpStatus.BAD_REQUEST);
        }
        try {
            landRegistryService.updatePostcodeDatabase(prefix);
            return new ResponseEntity<>("Update triggered", HttpStatus.OK);
        } catch (IOException | UnirestException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    private List<HashMap<String, String>> getLocationDataKeys(List<LandRegistryData> landRegistryDataList) {
        List<HashMap<String, String>> keys = new ArrayList<>();
        for (LandRegistryData data : landRegistryDataList) {
            keys.add(data.getMappings());
        }
        return keys;
    }

    private String formatPostCode(String postCode) {
        if (postCode.charAt(postCode.length() - 4) == 32) {
            return postCode.toUpperCase();
        } else if (postCode.length() > 3) {
            return (postCode.substring(0, postCode.length() - 3) + " " + postCode.substring(postCode.length() - 3)).toUpperCase();
        } else {
            throw new InvalidParameterException("Postcode " + postCode + "is too short");
        }
    }
}
