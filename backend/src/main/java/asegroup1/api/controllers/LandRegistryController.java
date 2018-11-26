package asegroup1.api.controllers;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

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


    private LandRegistryServiceImpl landRegistryService;


    @Autowired
    public LandRegistryController(LandRegistryServiceImpl landRegistryService) {
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
        return new ResponseEntity<>("[\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"id\": \"1\",\n" +
                "      \"paon\": \"9\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8124399\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"610000\",\n" +
                "      \"transactionDate\": \"2016-08-12\",\n" +
                "      \"longitude\": \"-0.3795235\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"4\",\n" +
                "      \"id\": \"2\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8119689\",\n" +
                "      \"saon\": \"FLAT 2\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"112500\",\n" +
                "      \"transactionDate\": \"2015-03-19\",\n" +
                "      \"longitude\": \"-0.3791697\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"13\",\n" +
                "      \"id\": \"3\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8126867\",\n" +
                "      \"saon\": \"FLAT 2\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"189000\",\n" +
                "      \"transactionDate\": \"2014-09-12\",\n" +
                "      \"longitude\": \"-0.3796079\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"4\",\n" +
                "      \"id\": \"4\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8119689\",\n" +
                "      \"saon\": \"FLAT 6\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"155000\",\n" +
                "      \"transactionDate\": \"2016-12-22\",\n" +
                "      \"longitude\": \"-0.3791697\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"4\",\n" +
                "      \"id\": \"5\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8119689\",\n" +
                "      \"saon\": \"FLAT 7\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"120000\",\n" +
                "      \"transactionDate\": \"2015-04-24\",\n" +
                "      \"longitude\": \"-0.3791697\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"4\",\n" +
                "      \"id\": \"6\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8119689\",\n" +
                "      \"saon\": \"FLAT 5\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"224500\",\n" +
                "      \"transactionDate\": \"2017-09-22\",\n" +
                "      \"longitude\": \"-0.3791697\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"SUSSEX COURT\",\n" +
                "      \"id\": \"7\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8128956\",\n" +
                "      \"saon\": \"FLAT 2\",\n" +
                "      \"postcode\": \"BN11 4BT\",\n" +
                "      \"pricePaid\": \"155000\",\n" +
                "      \"transactionDate\": \"2016-10-07\",\n" +
                "      \"longitude\": \"-0.3797229\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"4\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"id\": \"8\",\n" +
                "      \"latitude\": \"50.8119689\",\n" +
                "      \"saon\": \"FLAT 8\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"170000\",\n" +
                "      \"transactionDate\": \"2015-05-20\",\n" +
                "      \"longitude\": \"-0.3791697\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"15\",\n" +
                "      \"id\": \"9\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8128531\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"490000\",\n" +
                "      \"transactionDate\": \"2016-10-20\",\n" +
                "      \"longitude\": \"-0.3796054\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"HARDWICKE LODGE\",\n" +
                "      \"id\": \"10\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8125322\",\n" +
                "      \"saon\": \"FLAT 10\",\n" +
                "      \"postcode\": \"BN11 4BU\",\n" +
                "      \"pricePaid\": \"100000\",\n" +
                "      \"transactionDate\": \"2018-09-04\",\n" +
                "      \"longitude\": \"-0.3790423\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"mappings\": {\n" +
                "      \"town\": \"WORTHING\",\n" +
                "      \"paon\": \"11\",\n" +
                "      \"id\": \"11\",\n" +
                "      \"street\": \"TENNYSON ROAD\",\n" +
                "      \"latitude\": \"50.8125689\",\n" +
                "      \"saon\": \"FLAT 1\",\n" +
                "      \"postcode\": \"BN11 4BY\",\n" +
                "      \"pricePaid\": \"200000\",\n" +
                "      \"transactionDate\": \"2016-05-27\",\n" +
                "      \"longitude\": \"-0.3795491\"\n" +
                "    }\n" +
                "  }\n" +
                "]", HttpStatus.OK);
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
