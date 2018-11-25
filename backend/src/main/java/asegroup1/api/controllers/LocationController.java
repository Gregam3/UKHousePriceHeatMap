package asegroup1.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import asegroup1.api.models.LocationData;
import asegroup1.api.services.location.LocationServiceImpl;

/**
 * @author James Fernando, Rikkey Paal
 */

@RestController
@RequestMapping("/location/")
public class LocationController {

    private LocationServiceImpl locationService;

    @Autowired
    public LocationController(LocationServiceImpl locationService) {
        this.locationService = locationService;
    }

    @PostMapping(value = {"add-location-data", "add-location-data/"})
    public ResponseEntity<String> postAddLocationData(@RequestBody LocationData location) {
        locationService.create(location);
        return new ResponseEntity<>("Successfully added to database", HttpStatus.OK);
    }

    @GetMapping(value = {"get-user-locations/{user-id}"})
    public ResponseEntity<List<LocationData>> getUserLocation(@PathVariable("user-id") String userID) {
        return new ResponseEntity<List<LocationData>>(locationService.getLocationData(userID), HttpStatus.OK);
    }
}
