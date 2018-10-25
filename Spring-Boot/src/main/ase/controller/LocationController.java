package ase.controller;

import main.ase.DataEntities.Location;
import org.springframework.web.bind.annotation.*;

@RestController
public class LocationController {

    @RequestMapping(value="/location", method = RequestMethod.POST)
    public @ResponseBody String post(@RequestHeader String userID,@RequestBody Location location) {
        return "Successful Response";
    }
}
