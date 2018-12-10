package asegroup1.api.controllers;

import asegroup1.api.services.landregistry.LandRegistryServiceImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@RestController
@RequestMapping("/land-registry/")
@Api(value = "Land registry data", description = "Operations pertaining to Land Registry data")
public class LandRegistryController {

    private final static Logger logger = Logger.getLogger(LandRegistryController.class.getName());
    private Properties mockResponses;
    private LandRegistryServiceImpl landRegistryService;

    @Autowired
    public LandRegistryController(LandRegistryServiceImpl landRegistryService) {
        try {
            FileInputStream fakeResponsesInputStream = new FileInputStream(new File("src/main/java/asegroup1/api/controllers/fake-responses.properties"));
            mockResponses = new Properties();
            mockResponses.load(fakeResponsesInputStream);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to setup fake responses", e);
        }
        this.landRegistryService = landRegistryService;
    }

    @ApiOperation(value = "Get Land registry data based on map position")
    @GetMapping("get-display-data")
    public ResponseEntity<?> getDataToDisplayOnMap(@RequestParam JSONObject mapPosition) {
        long timer = System.currentTimeMillis();

        try {
            for (String jsonKey : new String[]{"top", "bottom", "left", "right"}) {
                if (mapPosition.isNull(jsonKey))
                    throw new InvalidParameterException("Value \"" + jsonKey + "\" could not be found, please ensure the request body contains this value as a top level node");
            }

            List<?> positionsInsideBounds = landRegistryService.getPositionInsideBounds(mapPosition);

            logger.log(Level.INFO,
                    "\n-----------------------------------------------------------------------------------------------------\n" +
                            "\t\t\t\t\t\t\tRequest took " + (System.currentTimeMillis() - timer) + "ms to fetch " + positionsInsideBounds.size() + " elements \n " +
                            "-----------------------------------------------------------------------------------------------------"
            );

            return new ResponseEntity<>(positionsInsideBounds, HttpStatus.OK);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error Getting Land Registry Data", e);
            return new ResponseEntity<>("An error occurred whilst handling this request: " + e, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Get test data sample")
    @GetMapping(value = "get-display-data-test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTestDisplayData() {
        return new ResponseEntity<>(mockResponses.getProperty("addressData"), HttpStatus.OK);
    }

    @ApiOperation(value = "Updates average price for each postcode with defined prefix")
    @GetMapping("update-postcode/{prefix}")
    public ResponseEntity<?> updateTransactionData(String prefix) {
        if (prefix == null) {
            prefix = "";
        } else if (!prefix.matches("[\\p{Alnum} ]+")) {
            return new ResponseEntity<>("Invalid postcode pattern", HttpStatus.BAD_REQUEST);
        }
        try {
            landRegistryService.updatePostcodeDatabase(prefix);
            return new ResponseEntity<>("Update triggered", HttpStatus.OK);
        } catch (IOException | UnirestException e) {
            logger.log(Level.SEVERE, "Failure to Update Database", e);
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }
}
