package asegroup1.api.controllers;

import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.services.landregistry.LandRegistryServiceImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@RestController
@RequestMapping("/land-registry/")
public class LandRegistryController {


    private LandRegistryServiceImpl landRegistryService;
    private static final int YEARS_TO_FETCH = 5;

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
        long timer = System.nanoTime();

        try {
            for (String jsonKey : new String[]{"top", "bottom", "left", "right"}) {
                if (mapPosition.isNull(jsonKey))
                    throw new InvalidParameterException("Value " + jsonKey + " could not be found, please ensure requestbody contains this value as a top level node");
            }

            List<?> positionsInsideBounds = landRegistryService.getPositionInsideBounds(mapPosition);

            System.out.println(
                    "\n-----------------------------------------------------------------------------------------------------\n" +
                            "\t\t\t\t\t\t\tRequest took " + (System.nanoTime() - timer) / 1000000 + "ms to fetch " + positionsInsideBounds.size() + " elements \n " +
                            "-----------------------------------------------------------------------------------------------------"
            );

            return new ResponseEntity<>(positionsInsideBounds, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("{\"error\" :\"An error occurred whilst handling this request: " + e +"\"}", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("get-transactions/{post-code}")
    public ResponseEntity<?> getTransactionDataForPostCode(@PathVariable("post-code") String postCode) {
        LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
        constraint.getEqualityConstraints().setPostCode(formatPostCode(postCode));
        constraint.setMinDate(LocalDate.now().minusYears(5));

        try {
            return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getLatestTransactions(new ArrayList<>(EnumSet.allOf(Selectable.class)), constraint)),
                    HttpStatus.OK);
        } catch (IOException | UnirestException | ParseException e) {
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
        } else {
            return (postCode.substring(0, postCode.length() - 3) + " " + postCode.substring(postCode.length() - 3)).toUpperCase();
        }
    }
}
