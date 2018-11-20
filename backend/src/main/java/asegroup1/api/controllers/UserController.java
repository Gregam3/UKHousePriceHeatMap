package asegroup1.api.controllers;

import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
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

	@PostMapping(value = {"add-user-data","add-user-data/"})
	public ResponseEntity<String> post(@RequestBody UserData userData) {
		userService.create(userData);
		return new ResponseEntity<>("User Added", HttpStatus.OK);
	}
}
