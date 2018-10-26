package main.ase.controller;

import main.ase.DataEntities.User;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
  @RequestMapping(value = "/user", method = RequestMethod.POST)
  public @ResponseBody String post(@RequestBody User user) {
    return "Successfully added";
  }
}
