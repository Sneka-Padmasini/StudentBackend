package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SignInService signInService;

    Map<String, String> map = new HashMap<>();

    @PostMapping("/register/newUser")
    public ResponseEntity<?> addNewUser(
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("mobile") String mobile,
            @RequestParam("dob") String dob,
            @RequestParam("gender") String gender,
            @RequestParam("selectedCourses") String selectedCoursesJson,
            @RequestParam("selectedStandard") String selectedStandardJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            
            // 🔥 ADD THESE 3 LINES 🔥
            @RequestParam(value = "plan", required = false, defaultValue = "trial") String plan,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            
            @RequestParam(value = "paymentId", required = false) String paymentId,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "amountPaid", required = false) String amountPaid,
            @RequestParam(value = "payerId", required = false) String payerId
    ) {
        Map<String, String> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<String> selectedCourses = mapper.readValue(selectedCoursesJson, new TypeReference<List<String>>() {});
            List<String> selectedStandards = mapper.readValue(selectedStandardJson, new TypeReference<List<String>>() {});

            Map<String, List<String>> courseMap = new HashMap<>();
            for (String course : selectedCourses) {
                courseMap.put(course, selectedStandards);
            }

            UserModel user = new UserModel();
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setPassword(password);
            user.setMobile(mobile);
            user.setDob(dob);
            user.setGender(gender);
            user.setSelectedCourse(courseMap); 
            user.setSelectedStandard(selectedStandards);
            user.setCourseName(String.join(", ", selectedCourses));
            user.setCoursetype(String.join(", ", selectedCourses));

            // 🔥 SAVE THE PLAN INFO 🔥
            user.setPlan(plan);
            user.setStartDate(startDate);
            user.setEndDate(endDate);
            
         // 🔥 SAVE THE PAYMENT DETAILS 🔥
            user.setPaymentId(paymentId);
            user.setPaymentMethod(paymentMethod);
            user.setAmountPaid(amountPaid);
            
            user.setPayerId(payerId);

            if (photo != null && !photo.isEmpty()) {
                user.setPhoto(photo.getOriginalFilename());
            }

            UserDTO dto = new UserDTO();
            dto.setUser(user);

            boolean saved = userService.saveNewUser(dto);
            if (saved) {
                map.put("status", "pass");
                map.put("message", "User registered successfully");
            } else {
                map.put("status", "failed");
                map.put("message", "User with this email already exists");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", "failed");
            map.put("message", "Error processing request");
        }

        return ResponseEntity.ok(map);
    }

    @GetMapping("/getUsers")
    public List<UserModel> getUsers() {
        return userService.getUsers();
    }



    @PutMapping("/upgradePlan/{email}")
    public ResponseEntity<?> upgradePlan(@RequestBody UserModel user, @PathVariable("email") String email) {
        Map<String, String> map = new HashMap<>();
        
        // Call the service method we just created
        boolean updated = signInService.upgradeUserPlan(email, user);

        if (updated) {
            map.put("status", "pass");
            map.put("message", "Plan upgraded successfully");
            return ResponseEntity.ok(map);
        } else {
            map.put("status", "failed");
            map.put("message", "Failed to upgrade plan");
            return ResponseEntity.status(500).body(map);
        }
    }

    @DeleteMapping("/deleteUser/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable("email") String email) {
        userService.deleteUser(email);
        map.put("status", "pass");
        return ResponseEntity.ok(map);
    }
}
