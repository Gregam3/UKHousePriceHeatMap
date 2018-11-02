package asegroup1.api.controllers;

import asegroup1.api.models.LandRegistryData;
import asegroup1.api.services.landregistry.LandRegistryServiceImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;


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
        //Checks if postcode has space in correct place in order to avoid error when it is split later on
        if (postCode.charAt(postCode.length() - 4) != 32) {
            return new ResponseEntity<>("Post code must contain a space in the correct position and be in a valid format http://www.restore.ac.uk/geo-refer/38330mtuks00y19740000.php", HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(landRegistryService.getLandRegistryDataByPostCode(postCode), HttpStatus.OK);
        } catch (UnirestException | IOException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("get-transactions/{post-code}")
    public ResponseEntity<?> getTransactionDataForPostCode(@PathVariable("post-code") String postCode) {
        try {
            return new ResponseEntity<>(landRegistryService.getTransactionsForPostCode(postCode), HttpStatus.OK);
        } catch (IOException | UnirestException | ParseException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }
}
