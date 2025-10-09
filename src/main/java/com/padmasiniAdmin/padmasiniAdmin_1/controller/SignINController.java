package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UserDetails;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class SignINController {

    @Autowired
    private SignInService signInService;

    // ✅ LOGIN
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody UserDetails user, HttpSession session, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();

        UserModel checkUser = signInService.checkUserEmail(user.getUserName(), user.getPassword());

        if (checkUser == null) {
            map.put("status", "failed");
        } else {
            map.put("status", "pass");

            // Compose userName
            String userName = (checkUser.getFirstname() != null ? checkUser.getFirstname() : "") + 
                              " " + 
                              (checkUser.getLastname() != null ? checkUser.getLastname() : "");
            map.put("userName", userName.trim());
            map.put("firstName", checkUser.getFirstname());
            map.put("lastName", checkUser.getLastname());
            map.put("email", checkUser.getEmail());
            map.put("phoneNumber", checkUser.getMobile());
            map.put("role", checkUser.getRole() != null ? checkUser.getRole() : "student");

            // Build courseName / coursetype from selectedCourse
            Map<String, List<String>> selectedCourses = checkUser.getSelectedCourse();
            String courseName = "";
            if (selectedCourses != null && !selectedCourses.isEmpty()) {
                List<String> courseList = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : selectedCourses.entrySet()) {
                    String standardsStr = entry.getValue() != null ? String.join(",", entry.getValue()) : "";
                    courseList.add(entry.getKey() + (standardsStr.isEmpty() ? "" : " (" + standardsStr + ")"));
                }
                courseName = String.join(", ", courseList);
            }
            map.put("courseName", courseName);
            map.put("coursetype", courseName);

            // ✅ Flatten standards
            List<String> standards = new ArrayList<>();
            if (selectedCourses != null && !selectedCourses.isEmpty()) {
                for (List<String> stdList : selectedCourses.values()) {
                    if (stdList != null) standards.addAll(stdList);
                }
            }
            if (checkUser.getSelectedStandard() != null && !checkUser.getSelectedStandard().isEmpty()) {
                standards.addAll(checkUser.getSelectedStandard());
            }
            // remove duplicates
            Set<String> uniqueStandards = new HashSet<>(standards);
            map.put("standards", new ArrayList<>(uniqueStandards));
            session.setAttribute("standards", new ArrayList<>(uniqueStandards));

            // ✅ Ensure subjects are not null
            List<String> subjects = checkUser.getSubjects() != null ? checkUser.getSubjects() : new ArrayList<>();
            map.put("subjects", subjects);
            session.setAttribute("subjects", subjects);

            // Other fields
            map.put("photo", checkUser.getPhoto());
            map.put("dob", checkUser.getDob());
            map.put("gender", checkUser.getGender());
            map.put("isVerified", checkUser.getIsVerified());

            // Save session info
            session.setAttribute("user", userName.trim());
            session.setAttribute("email", checkUser.getEmail());
            session.setAttribute("phoneNumber", checkUser.getMobile());
            session.setAttribute("role", checkUser.getRole() != null ? checkUser.getRole() : "student");
            session.setAttribute("courseName", courseName);
            session.setAttribute("coursetype", courseName);

            // Cookie
            Cookie cookie = new Cookie("email", checkUser.getEmail());
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);

            System.out.println("✅ Logged in user: " + checkUser.getEmail());
        }

        return ResponseEntity.ok(map);
    }

    // ✅ LOGOUT
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();

        if (session.getAttribute("user") != null) {
            session.invalidate();

            Cookie cookie = new Cookie("email", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            map.put("message", "Logged out successfully"); 
        } else {
            map.put("message", "No active session");
        }

        return ResponseEntity.ok(map);
    }

    // ✅ CHECK SESSION
    @GetMapping("/checkSession")
    public ResponseEntity<?> checkSession(HttpSession session, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("🧩 Checking session: " + session.getId());

        if (session.getAttribute("user") == null) {
            System.out.println("⚠️ Session expired or user not logged in.");
            session.invalidate();

            Cookie cookie = new Cookie("email", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            map.put("status", "failed");
        } else {
            map.put("status", "pass");
            map.put("userName", session.getAttribute("user"));
            map.put("email", session.getAttribute("email"));
            map.put("phoneNumber", session.getAttribute("phoneNumber"));
            map.put("role", session.getAttribute("role"));
            map.put("coursetype", session.getAttribute("coursetype"));
            map.put("courseName", session.getAttribute("courseName"));
            map.put("subjects", session.getAttribute("subjects"));
            map.put("standards", session.getAttribute("standards"));

            System.out.println("✅ Active session for: " + session.getAttribute("email"));
        }

        return ResponseEntity.ok(map);
    }
}
