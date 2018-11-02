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


@RestController
@RequestMapping("/land-registry/")
public class LandRegistryController {

    private LandRegistryServiceImpl landRegistryService;

    @Autowired
    public LandRegistryController(LandRegistryServiceImpl landRegistryService) {
        this.landRegistryService = landRegistryService;
    }

    @GetMapping("get/{post-code}")
    public ResponseEntity<?> getLandRegistryDataForPostCode(@PathVariable("post-code") String postCode) {
        if (!postCode.contains(" ")) {
            return new ResponseEntity<>("Post code must contain a space", HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(landRegistryService.getLandRegistryDataByPostCode(postCode), HttpStatus.OK);
        } catch (UnirestException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }
}
