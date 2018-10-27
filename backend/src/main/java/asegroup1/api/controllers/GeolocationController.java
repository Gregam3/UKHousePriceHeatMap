package asegroup1.api.controllers;

import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class GeolocationController {

    private UserServiceImpl userService;

    @Autowired
    public GeolocationController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping("/")
    public String locationMock() {
        UserData test = userService.get("WHATEVER");

        return test.getUserId();
    }
}