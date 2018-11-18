package asegroup1.api.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import asegroup1.api.services.landregistry.LandRegistryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;


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

	@GetMapping("get-transactions/{post-code}")
	public ResponseEntity<?> getTransactionDataForPostCode(@PathVariable("post-code") String postCode) {
		LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
		constraint.getEqualityConstraints().setPostCode(formatPostCode(postCode));
		constraint.setMinDate(LocalDate.now().minusYears(5));

		try {
			return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getTransactions(constraint, true)), HttpStatus.OK);
		} catch (IOException | UnirestException e) {
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
