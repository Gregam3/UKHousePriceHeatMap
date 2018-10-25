package asegroup1.api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class GeolocationController {

    @RequestMapping("/")
    public String locationMock() {
        return "[EXAMPLE COORDINATES]";
    }
}
