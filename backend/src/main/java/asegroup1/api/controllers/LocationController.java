package asegroup1.api.controllers;

import asegroup1.api.models.LocationData;
import asegroup1.api.services.location.LocationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/location/")
public class LocationController {

	private LocationServiceImpl locationService;

	@Autowired
	public LocationController(LocationServiceImpl locationService) {
		this.locationService = locationService;
	}

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	String post(@RequestBody LocationData location) {
		locationService.create(location)
		return new ResponseEntity<String>("successfully added to databse", HttpStatus.OK);
	}
}
