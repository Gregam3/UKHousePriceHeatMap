package asegroup1.api.controllers;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.api.scripting.JSObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import asegroup1.api.models.LocationData;
import asegroup1.api.services.location.LocationServiceImpl;
import asegroup1.api.services.user.UserServiceImpl;

/**
 * @author James Fernando, Rikkey Paal
 */

@RestController
@RequestMapping("/location/")
@Api(value="User location", description = "Operations related to user location data being sent to the server")
public class LocationController {

	private UserServiceImpl userService;
    private LocationServiceImpl locationService;

    @Autowired
	public LocationController(LocationServiceImpl locationService, UserServiceImpl userServiceImpl) {
        this.locationService = locationService;
		this.userService = userServiceImpl;
    }

    @ApiOperation(value= "Add user location data to the server")
    @PostMapping(value = {"add-location-data", "add-location-data/"})
    public ResponseEntity<String> postAddLocationData(@RequestBody LocationData location) {
		if (userService.get(location.getUserId()) != null) {
			try {
				locationService.create(location);
				return new ResponseEntity<>("Successfully added to database", HttpStatus.OK);
			} catch (InvalidParameterException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>("Could not add to database: User does not exist", HttpStatus.BAD_REQUEST);
		}

    }

    @ApiOperation(value= "Get user location data")
    @GetMapping(value = {"get-user-locations/{user-id}"})
    public ResponseEntity<List<LocationData>> getUserLocation(@PathVariable("user-id") String userID) {
        return new ResponseEntity<>(locationService.getLocationData(userID), HttpStatus.OK);
    }

    @GetMapping(value = "get-address-coordinates/{address}", produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<?> getAddressCoordinates(@PathVariable("address") String address) {
		try {
			return new ResponseEntity<>(locationService.getAddressCoordinates(address), HttpStatus.OK);
		} catch (UnirestException | IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Could not retrieve address: " + e, HttpStatus.BAD_REQUEST);
		}
	}

}
