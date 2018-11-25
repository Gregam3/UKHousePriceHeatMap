package asegroup1.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@RestController
@RequestMapping("/user/")
public class UserController {

	private UserServiceImpl userService;

	@Autowired
	public UserController(UserServiceImpl userService) {
		this.userService = userService;
	}

	@GetMapping(value = "get-user-data/{userid}")
	public ResponseEntity<UserData> getUserData(@PathVariable("userid") String userid) {
		return new ResponseEntity<>(userService.get(userid), HttpStatus.OK);
	}

	@GetMapping(value = { "add-user-data/{key}" })
	public ResponseEntity<String> addUserData(@PathVariable String key) {
		System.out.println(key);
		switch (userService.add(key)) {
			case 0:
				return new ResponseEntity<>("User Added", HttpStatus.OK);
			case -1:
				return new ResponseEntity<>("User already exists", HttpStatus.BAD_REQUEST);
			case 1:
				return new ResponseEntity<>("User was not Added", HttpStatus.BAD_REQUEST);
			default:
				return new ResponseEntity<>("An unexpected error occured", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "check-user-exsists/{userid}")
	public ResponseEntity<Boolean> checkUserData(@PathVariable("userid") String userid) {
		UserData user = userService.get(userid);
		return new ResponseEntity<>(user != null, HttpStatus.OK);
	}
}
