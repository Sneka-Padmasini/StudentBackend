package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime; 
import java.time.Duration;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ✅ Models
import com.padmasiniAdmin.padmasiniAdmin_1.model.UserDetails;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.model.StudySession;

// ✅ Services
import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;
import com.padmasiniAdmin.padmasiniAdmin_1.service.EmailService;
import com.padmasiniAdmin.padmasiniAdmin_1.service.StudyTrackerService;

// ✅ Repositories
import com.padmasiniAdmin.padmasiniAdmin_1.repository.StudySessionRepository;

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
    
    @Autowired 
    private StudySessionRepository sessionRepo;
    
    @Autowired 
    private StudyTrackerService trackerService;


    // ================= ROOT ENDPOINT =================
    @GetMapping("/")
    public String home() {
        return "Padmasini Admin Backend is running! Server Time: " + new java.util.Date();
    }

    // ================= LOGIN =================
 // ================= LOGIN =================
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody UserDetails user, HttpSession session, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();

        // 1. Check User Credentials via Service
        UserModel checkUser = signInService.checkUserEmail(user.getUserName(), user.getPassword());

        if (checkUser == null) {
            map.put("status", "failed");
        } else {
            map.put("status", "pass");
            
            // ✅ Add User ID
            map.put("userId", checkUser.getId());
            
            // ✅ IMPROVED STUDY SESSION LOGIC (Consolidated)
            try {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
                
                // Fetch today's sessions for this user
                List<StudySession> existingSessions = sessionRepo.findByUserIdAndLoginTimeBetween(
                    checkUser.getId(), startOfDay, endOfDay);
                
                StudySession currentSession = null;
                
                // Check if there is an OPEN session (logoutTime is null)
                for (StudySession s : existingSessions) {
                    if (s.getLogoutTime() == null) {
                        currentSession = s;
                        break;
                    }
                }
                
                // If no open session found, check if we can resume the latest closed session (Optional, but cleaner)
                // For now, let's stick to your requirement: Resuming only if open, otherwise create new.
                
                if (currentSession == null) {
                    // Create new session
                    currentSession = new StudySession(checkUser.getId(), now);
                } else {
                    // Resume existing open session (update login time to now is optional, usually we keep original start time)
                    // But to be safe, let's ensure we have the ID reference.
                }
                
                StudySession savedSession = sessionRepo.save(currentSession);
                
                // Send Session ID to frontend so we can close it later on logout
                map.put("sessionId", savedSession.getId()); 
                session.setAttribute("currentSessionId", savedSession.getId());
                
            } catch (Exception e) {
                System.out.println("⚠️ Warning: Could not save study session: " + e.getMessage());
            }

            // ---------- User Basic Info (Your Original Logic) ----------
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
            map.put("plan", checkUser.getPlan());
            map.put("startDate", checkUser.getStartDate());
            map.put("endDate", checkUser.getEndDate());

            // ---------- Save session info ----------
            session.setAttribute("user", userName.trim());
            session.setAttribute("userId", checkUser.getId());
            session.setAttribute("email", checkUser.getEmail());
            session.setAttribute("phoneNumber", checkUser.getMobile());
            session.setAttribute("role", checkUser.getRole() != null ? checkUser.getRole() : "student");
            session.setAttribute("courseName", courseNameStr);
            session.setAttribute("coursetype", courseNameStr);
            session.setAttribute("standards", new ArrayList<>(standardsSet));
            session.setAttribute("subjects", subjects);
            
            session.setAttribute("gender", checkUser.getGender());
            session.setAttribute("isVerified", checkUser.getIsVerified());
            session.setAttribute("plan", checkUser.getPlan());
            session.setAttribute("startDate", checkUser.getStartDate());
            session.setAttribute("endDate", checkUser.getEndDate());
            session.setAttribute("paymentId", checkUser.getPaymentId());
            session.setAttribute("payerId", checkUser.getPayerId());

            // ---------- Cookie ----------
            Cookie cookie = new Cookie("email", checkUser.getEmail());
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);

            System.out.println("✅ Logged in user: " + checkUser.getEmail() + " | Session ID: " + map.get("sessionId"));
        }

        return ResponseEntity.ok(map);
    }

    // ================= LOGOUT =================
    @PostMapping("/logout") 
    public ResponseEntity<?> logout(@RequestBody(required=false) Map<String, String> request, HttpSession session, HttpServletResponse response) {
        
        // 1. Get Session ID from React (body) or internal Session
        String sessionId = (request != null) ? request.get("sessionId") : (String) session.getAttribute("currentSessionId");

        System.out.println("🛑 Logout Requested for Session ID: " + sessionId); 

        if (sessionId != null) {
            StudySession dbSession = sessionRepo.findById(sessionId).orElse(null);
            
            if (dbSession != null) {
                // Only update if it hasn't been closed yet
                if (dbSession.getLogoutTime() == null) {
                    dbSession.setLogoutTime(LocalDateTime.now());
                    
                    // ✅ FIXED TIME CALCULATION (Prevents 0 minutes)
                    long seconds = Duration.between(dbSession.getLoginTime(), dbSession.getLogoutTime()).getSeconds();
                    
                    long minutes = seconds / 60;
                    
                    // If studied less than 60s but more than 5s, round up to 1 min so it counts.
                    if (minutes == 0 && seconds > 0) {
                        minutes = 1; 
                    }
                    
                    dbSession.setDurationInMinutes(minutes);
                    sessionRepo.save(dbSession);
                    System.out.println("✅ Study Session Ended. Duration: " + minutes + " mins (Seconds: " + seconds + ")");
                } else {
                    System.out.println("⚠️ Session was already closed previously.");
                }
            } else {
                System.out.println("❌ Error: Session ID found in request but NOT in Database.");
            }
        } else {
            System.out.println("❌ Error: No Session ID provided in logout request.");
        }

        // 3. Clear Session
        session.invalidate();
        Cookie cookie = new Cookie("email", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ================= CALCULATE PLAN (New Feature) =================
    @PostMapping("/calculate-plan")
    public ResponseEntity<?> calculatePlan(@RequestBody Map<String, Integer> request) {
        // Default to 3 hours if input is null
        Integer hours = request.get("hours");
        if(hours == null) hours = 3; 
        
        return ResponseEntity.ok(trackerService.calculateStudyPlan(hours));
    }
    
    // ================= CHECK SESSION =================
    @GetMapping("/checkSession")
    public ResponseEntity<?> checkSession(HttpSession session, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();

        if (session.getAttribute("user") == null) {
            session.invalidate();

            Cookie cookie = new Cookie("email", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            map.put("status", "failed");
        } else {
            map.put("status", "pass");
            map.put("userId", session.getAttribute("userId"));
            map.put("_id", session.getAttribute("userId"));
            map.put("userName", session.getAttribute("user"));
            map.put("email", session.getAttribute("email"));
            map.put("phoneNumber", session.getAttribute("phoneNumber"));
            map.put("role", session.getAttribute("role"));
            map.put("coursetype", session.getAttribute("coursetype"));
            map.put("courseName", session.getAttribute("courseName"));
            map.put("subjects", session.getAttribute("subjects"));
            map.put("standards", session.getAttribute("standards"));
            
            map.put("plan", session.getAttribute("plan"));
            map.put("startDate", session.getAttribute("startDate"));
            map.put("endDate", session.getAttribute("endDate"));
            map.put("paymentId", session.getAttribute("paymentId"));
            map.put("payerId", session.getAttribute("payerId"));
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
