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
import com.padmasiniAdmin.padmasiniAdmin_1.service.EmailService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class SignINController {

    @Autowired
    private SignInService signInService;

    @Autowired
    private EmailService emailService;


    // ================= ROOT ENDPOINT =================
@GetMapping("/")
public String home() {
    return "Padmasini Admin Backend is running! Server Time: " + new java.util.Date();
}

    // ================= LOGIN =================
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody UserDetails user, HttpSession session, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();

        UserModel checkUser = signInService.checkUserEmail(user.getUserName(), user.getPassword());

        if (checkUser == null) {
            map.put("status", "failed");
        } else {
            map.put("status", "pass");
            
         // ✅ Add this line (Important)
            map.put("userId", checkUser.getId());

            // ---------- User Basic Info ----------
            String userName = (checkUser.getFirstname() != null ? checkUser.getFirstname() : "") +
                              " " +
                              (checkUser.getLastname() != null ? checkUser.getLastname() : "");
            map.put("userName", userName.trim());
            map.put("firstName", checkUser.getFirstname());
            map.put("lastName", checkUser.getLastname());
            map.put("email", checkUser.getEmail());
            map.put("phoneNumber", checkUser.getMobile());
            map.put("role", checkUser.getRole() != null ? checkUser.getRole() : "student");

            // ---------- Courses ----------
            Map<String, List<String>> selectedCourses = checkUser.getSelectedCourse();
            List<String> courseNames = new ArrayList<>();
            if (selectedCourses != null && !selectedCourses.isEmpty()) {
                courseNames.addAll(selectedCourses.keySet());
            }

            String courseNameStr = String.join(", ", courseNames);
            map.put("courseName", courseNameStr);
            map.put("coursetype", courseNameStr);

            // ---------- Standards ----------
            Set<String> standardsSet = new HashSet<>();
            if (selectedCourses != null && !selectedCourses.isEmpty()) {
                for (List<String> stdList : selectedCourses.values()) {
                    if (stdList != null) standardsSet.addAll(stdList);
                }
            }

            // Also include selectedStandard list directly (if present)
            if (checkUser.getSelectedStandard() != null) {
                standardsSet.addAll(checkUser.getSelectedStandard());
            }

            map.put("standards", new ArrayList<>(standardsSet));

            // ---------- Subjects ----------
            List<String> subjects = checkUser.getSubjects() != null ? checkUser.getSubjects() : new ArrayList<>();
            map.put("subjects", subjects);

            // ---------- Other fields ----------
            map.put("photo", checkUser.getPhoto());
            map.put("dob", checkUser.getDob());
            map.put("gender", checkUser.getGender());
            map.put("isVerified", checkUser.getIsVerified());

            // ---------- Save session info ----------
            session.setAttribute("user", userName.trim());
            session.setAttribute("email", checkUser.getEmail());
            session.setAttribute("phoneNumber", checkUser.getMobile());
            session.setAttribute("role", checkUser.getRole() != null ? checkUser.getRole() : "student");
            session.setAttribute("courseName", courseNameStr);
            session.setAttribute("coursetype", courseNameStr);
            session.setAttribute("standards", new ArrayList<>(standardsSet));
            session.setAttribute("subjects", subjects);

            // ---------- Cookie ----------
            Cookie cookie = new Cookie("email", checkUser.getEmail());
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);

            System.out.println("✅ Logged in user: " + checkUser.getEmail());
            System.out.println("✅ Courses: " + courseNameStr);
            System.out.println("✅ Standards: " + standardsSet);
        }

        return ResponseEntity.ok(map);
    }

    // ================= LOGOUT =================
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

    // ================= CHECK SESSION =================
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

    // ================= SEND OTP =================
    @PostMapping("/auth/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        int otp = (int)(Math.random() * 900000) + 100000;

        // Save OTP and expiry
        session.setAttribute("otp_" + email, otp);
        session.setAttribute("otp_exp_" + email, System.currentTimeMillis() + 5 * 60 * 1000);

        System.out.println("Generated OTP for " + email + ": " + otp);

        try {
            emailService.sendOtpEmail(email, otp);
            System.out.println("✅ OTP email sent successfully to " + email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to send OTP email"));
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/auth/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String otpStr = request.get("otp");

        if (email == null || otpStr == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }

        Object sessionOtp = session.getAttribute("otp_" + email);
        Object expiryObj = session.getAttribute("otp_exp_" + email);

        if (sessionOtp == null || expiryObj == null) {
            return ResponseEntity.status(400).body(Map.of("message", "OTP not found or expired"));
        }

        long expiryTime = (long) expiryObj;
        if (System.currentTimeMillis() > expiryTime) {
            session.removeAttribute("otp_" + email);
            session.removeAttribute("otp_exp_" + email);
            return ResponseEntity.status(400).body(Map.of("message", "OTP expired"));
        }

        if (otpStr.equals(sessionOtp.toString())) {
            session.removeAttribute("otp_" + email);
            session.removeAttribute("otp_exp_" + email);
            return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP"));
        }
    }
    
 // ================= FORGOT PASSWORD =================
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request, HttpSession session) {

        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        // Check if email exists
        UserModel user = signInService.checkEmailExists(email);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Email not found"));
        }

        // Generate OTP
        int otp = (int) (Math.random() * 900000) + 100000;

        // Store OTP temporarily
        session.setAttribute("reset_otp_" + email, otp);
        session.setAttribute("reset_otp_exp_" + email, System.currentTimeMillis() + 5 * 60 * 1000);

        try {
            // Send email
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok(Map.of("message", "Reset OTP sent to your email"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to send reset email"));
        }
    }

    @PostMapping("/auth/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestBody Map<String, String> request, HttpSession session) {

        String email = request.get("email");
        String otpStr = request.get("otp");

        if (email == null || otpStr == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }

        // Trim inputs to avoid whitespace errors
        email = email.trim();
        otpStr = otpStr.trim();

        Object sessionOtp = session.getAttribute("reset_otp_" + email);
        Object expiryObj = session.getAttribute("reset_otp_exp_" + email);

        if (sessionOtp == null || expiryObj == null) {
            return ResponseEntity.status(400).body(Map.of("message", "OTP not found or expired. Please request a new one."));
        }

        long expiryTime = (long) expiryObj;
        if (System.currentTimeMillis() > expiryTime) {
            session.removeAttribute("reset_otp_" + email);
            session.removeAttribute("reset_otp_exp_" + email);
            return ResponseEntity.status(400).body(Map.of("message", "OTP has expired"));
        }

        // Convert session OTP to String for comparison
        if (otpStr.equals(sessionOtp.toString())) {
            // OTP verified — allow password reset
            session.setAttribute("reset_verified_" + email, true);
            return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP entered"));
        }
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request, HttpSession session) {

        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and new password required"));
        }

        Boolean verified = (Boolean) session.getAttribute("reset_verified_" + email);
        if (verified == null || !verified) {
            return ResponseEntity.status(403).body(Map.of("message", "OTP not verified"));
        }

        // Update password using service
        boolean updated = signInService.updatePassword(email, newPassword);

        if (updated) {
            session.removeAttribute("reset_verified_" + email);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } else {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update password"));
        }
    }

    
}
