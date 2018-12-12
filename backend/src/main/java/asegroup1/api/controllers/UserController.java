package asegroup1.api.controllers;

import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@RestController
@RequestMapping("/user/")
@Api(value="User Data", description="Operations pertaining to user data")
public class UserController {

	private UserServiceImpl userService;
	private final static Logger logger = LogManager.getLogger(LandRegistryController.class);

	@Autowired
	public UserController(UserServiceImpl userService) {
		this.userService = userService;
	}

	@ApiOperation(value="Get the user's data")
	@GetMapping(value = "get-user-data/{userid}")
	public ResponseEntity<UserData> getUserData(@PathVariable("userid") String userid) {
		return new ResponseEntity<>(userService.get(userid), HttpStatus.OK);
	}

	@ApiOperation(value="Add the user's data")
	@GetMapping(value = { "add-user-data/{key}" })
	public ResponseEntity<String> addUserData(@PathVariable String key) {
		logger.info("Key: "+key);
		String message;
		switch (userService.add(key)) {
			case 0:
				message = "User Added";
				logger.info(message);
				return new ResponseEntity<>(message, HttpStatus.OK);
			case -1:
				message = "User already exists";
				logger.info(message);
				return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
			case 1:
				message = "User was not Added";
				logger.info(message);
				return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
			default:
				message = "An unexpected error occurred";
				logger.info(message);
				return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value="Check the user's ID")
	@GetMapping(value = "check-user-exists/{userid}")
	public ResponseEntity<Boolean> checkUserData(@PathVariable("userid") String userid) {
		UserData user = userService.get(userid);
		return new ResponseEntity<>(user != null, HttpStatus.OK);
	}
}
