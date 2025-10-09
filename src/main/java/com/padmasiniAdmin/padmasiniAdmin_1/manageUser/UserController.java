package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SignInService signInService;

    Map<String, String> map = new HashMap<>();

    @PostMapping("/newUser")
    public ResponseEntity<?> addNewUser(@RequestBody UserDTO user) {
        if(userService.saveNewUser(user)) map.put("status", "pass");
        else map.put("status", "failed");
        return ResponseEntity.ok(map);
    }

    @GetMapping("/getUsers")
    public List<UserModel> getUsers() {
        return userService.getUsers();
    }

    @PutMapping("/updateUser/{email}")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO user, @PathVariable("email") String email) {
        userService.deleteUser(email);
        userService.saveNewUser(user);
        map.put("status", "pass");
        return ResponseEntity.ok(map);
    }

    @DeleteMapping("/deleteUser/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable("email") String email) {
        userService.deleteUser(email);
        map.put("status", "pass");
        return ResponseEntity.ok(map);
    }
}
