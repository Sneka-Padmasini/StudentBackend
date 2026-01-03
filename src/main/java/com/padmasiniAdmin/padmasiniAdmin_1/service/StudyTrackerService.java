package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Progress;
import com.padmasiniAdmin.padmasiniAdmin_1.model.StudySession;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.ProgressRepository;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.StudySessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// ✅ THESE ARE THE MISSING IMPORTS FIXING YOUR ERROR
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudyTrackerService {

    @Autowired private StudySessionRepository sessionRepo;
    @Autowired private ProgressRepository progressRepo; 
    @Autowired private EmailService emailService;
    @Autowired private MongoClient mongoClient; 
    
    private final String dbName = "studentUsers";
    private final String collectionName = "studentUserDetail";

    private static final int WORKLOAD_HOURS_PER_LESSON = 20; 

    // 1. CALCULATE ESTIMATION
    public Map<String, Object> calculateStudyPlan(int hoursPerDay) {
        if (hoursPerDay <= 0) hoursPerDay = 1; 

        int totalLessons = 30; 
        int totalHoursRequired = totalLessons * WORKLOAD_HOURS_PER_LESSON;
        int estimatedDays = (int) Math.ceil((double) totalHoursRequired / hoursPerDay);
        int standardDays = (int) Math.ceil((double) totalHoursRequired / 4);

        Map<String, Object> response = new HashMap<>();
        response.put("totalLessons", totalLessons);
        response.put("estimatedDays", estimatedDays);
        response.put("message", "To finish " + totalLessons + " lessons (~" + totalHoursRequired + " hours), " +
                "studying " + hoursPerDay + " hrs/day will take you " + estimatedDays + " days. " +
                "(Standard pace: " + standardDays + " days at 4 hrs/day).");
        return response;
    }

    // 👉 FOR TESTING 
//    @EventListener(ApplicationReadyEvent.class)
    
    // 👉 FOR PRODUCTION 
    @Scheduled(cron = "0 59 23 * * ?", zone = "Asia/Kolkata")
    
    public void sendDailyReports() {
        System.out.println("🚀 STARTING DAILY REPORT GENERATION...");

        MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<UserModel> users = mongo.findAll(UserModel.class, collectionName);

        for (UserModel user : users) {
            
            // 1. Get Goal (Handle legacy users)
            int goal = user.getComfortableDailyHours();
            if (goal <= 0) goal = 3; 

            // 2. Calculate Study Time
            List<StudySession> sessions = sessionRepo.findByUserIdAndLoginTimeBetween(user.getId(), startOfDay, endOfDay);
            long totalMinutes = 0;
            for (StudySession s : sessions) {
                long sessionDur = s.getDurationInMinutes();
                if (sessionDur <= 0) continue; 
                if (sessionDur > 600) sessionDur = 600; 
                totalMinutes += sessionDur;
            }
            double actualHours = totalMinutes / 60.0;

            // 3. Get Last Learned Topic
            String lastTopic = "your lessons"; // Default fallback
            try {
                Progress prog = progressRepo.findByUserId(user.getId());
                if (prog != null && prog.getCompletedSubtopics() != null && !prog.getCompletedSubtopics().isEmpty()) {
                    for (String key : prog.getCompletedSubtopics().keySet()) {
                        if (!key.equals("course") && !key.equals("standard")) {
                            lastTopic = formatTopicName(key); 
                            break; 
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not fetch progress for user: " + user.getId());
            }

            // 4. Construct Email Content
            String subject = "Progress Update on Your Studies";
            String body = "";
            String studentName = (user.getFirstname() != null) ? user.getFirstname() : "Student";

            // LOGIC: Check if they met the time goal
            if (actualHours < goal) {
                // 🔴 LAGGING BEHIND (Detailed Formal Template)
                body = "Dear " + studentName + ",\n\n" +
                       "I am pleased to share your recent progress in studies:\n\n" +
                       "• You have shown improvement in understanding key concepts such as " + lastTopic + ".\n" +
                       "• Your participation reflects steady growth in confidence.\n" +
                       "• Areas to focus on: Time management. You targeted " + goal + " hours/day but achieved " + String.format("%.2f", actualHours) + " hours today.\n\n" +
                       "Keep up your dedication and continue practicing regularly. Remember, consistent effort is the key to success in exams like NEET.\n\n" +
                       "If you need clarification or guidance, feel free to reach out anytime.\n\n" +
                       "Best wishes,\n" +
                       "Learnforward\n" +
                       "Padmasini Innovations Pvt Ltd\n" +
                       "For support: learnforward@padmasini.com";
            } else {
                // 🟢 ON TRACK (Short Encouraging Template)
                body = "Hi " + studentName + ",\n\n" +
                       "Great job on your recent studies! You’ve improved in understanding topics like " + lastTopic + ".\n\n" +
                       "You also met your daily study goal of " + goal + " hours! 🎉\n\n" +
                       "Keep practicing regularly to strengthen speed in solving NEET Assessment.\n\n" +
                       "Best wishes,\n" +
                       "Learnforward\n" +
                       "Padmasini Innovations Pvt Ltd\n" +
                       "For support: learnforward@padmasini.com";
            }

            // 5. Send Email
            emailService.sendSimpleMessage(user.getEmail(), subject, body);
        }
        System.out.println("✅ DAILY REPORTS SENT.");
    }

    // Helper to clean up topic names
    private String formatTopicName(String dbKey) {
        try {
            String[] parts = dbKey.split("_");
            if (parts.length >= 3) {
                String rawTopic = dbKey.substring(dbKey.indexOf(parts[2])); 
                return rawTopic.replace("__dot__", ".").replace("_", " ");
            }
            return dbKey.replace("__dot__", ".").replace("_", " ");
        } catch (Exception e) {
            return dbKey;
        }
    }
}
