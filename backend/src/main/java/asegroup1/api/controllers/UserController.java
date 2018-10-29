package asegroup1.api.controllers;

import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

	private UserServiceImpl userService;

	@Autowired
	public UserController(UserServiceImpl userService) {
		this.userService = userService;
	}

	@RequestMapping(value = "/{userid}", method = RequestMethod.GET)
	public ResponseEntity<UserData> get(@PathVariable("userid") String userid) {
		return new ResponseEntity<>(userService.get(userid), HttpStatus.OK);
	}

	@RequestMapping(value = {"","/"}, method = RequestMethod.POST)
	public ResponseEntity<String> post(@RequestBody UserData userData) {
		userService.create(userData);
		return new ResponseEntity<>("User Added", HttpStatus.OK);
	}
}
