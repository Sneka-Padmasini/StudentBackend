package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Progress;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = {
        "https://d2i6kp2bub3ddu.cloudfront.net",
        "https://padmasini.com/Learnforward/",
        "http://localhost:5173",
        "https://padmasini.com",
        "https://studentfrontendpage.netlify.app",
        "https://padmasini7-frontend.netlify.app",
        "https://majestic-frangollo-031fed.netlify.app",
        "https://trilokinnovations.com",
        "https://www.trilokinnovations.com",
        "https://d2kr3vc90ue6me.cloudfront.net"
}, allowCredentials = "true")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepository;

    // ✅ Get progress by userId
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProgress(
        @PathVariable String userId,
        @RequestParam(required = false) String course,
        @RequestParam(required = false) String standard
    ) {
        try {
            Progress progress = progressRepository.findByUserId(userId);

            if (progress == null) {
                return ResponseEntity.ok(Map.of(
                    "completedSubtopics", Map.of(),
                    "subjectCompletion", Map.of()
                ));
            }

            // If course and standard are provided, filter the maps
            if (course != null && standard != null) {
                String prefix = (course + "_" + standard + "_").toLowerCase();
                
                // Filter completedSubtopics
                Map<String, Object> allTopics = progress.getCompletedSubtopics();
                Map<String, Object> filteredTopics = new HashMap<>();
                if (allTopics != null) {
                    allTopics.entrySet().stream()
                        .filter(entry -> entry.getKey().toLowerCase().startsWith(prefix))
                        .forEach(entry -> filteredTopics.put(entry.getKey(), entry.getValue()));
                }

                // Filter subjectCompletion
                Map<String, Integer> allSubjects = progress.getSubjectCompletion();
                Map<String, Integer> filteredSubjects = new HashMap<>();
                if (allSubjects != null) {
                    allSubjects.entrySet().stream()
                        .filter(entry -> entry.getKey().toLowerCase().startsWith(prefix))
                        .forEach(entry -> filteredSubjects.put(entry.getKey(), entry.getValue()));
                }

                // Return a new object with only the filtered data
                Progress filteredProgress = new Progress();
                filteredProgress.setUserId(userId);
                filteredProgress.setCompletedSubtopics(filteredTopics);
                filteredProgress.setSubjectCompletion(filteredSubjects);
                return ResponseEntity.ok(filteredProgress);
            }
            
            // If no course/standard provided, return everything
            return ResponseEntity.ok(progress);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading progress: " + e.getMessage());
        }
    }

    // ✅ Save or update progress
    
 // ProgressController.java (Around line 57)

    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody Progress newProgress) {
        if (newProgress.getUserId() == null || newProgress.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing userId in request body");
        }

        try {
            // 1️⃣ Find existing progress document
            Progress progressToSave = progressRepository.findByUserId(newProgress.getUserId());
            if (progressToSave == null) {
                progressToSave = new Progress();
                progressToSave.setUserId(newProgress.getUserId());
            }

            // 2️⃣ Extract course & standard from request body (sent from frontend)
            String course = newProgress.getCourse();
            String standard = newProgress.getStandard();

            if (course == null || course.isEmpty()) course = "NEET"; // fallback
            if (standard == null || standard.isEmpty()) standard = "11th"; // fallback

            // --- DEEP MERGE LOGIC FOR COMPLETED SUBTOPICS ---
            if (newProgress.getCompletedSubtopics() != null) {
                if (progressToSave.getCompletedSubtopics() == null) {
                    progressToSave.setCompletedSubtopics(new HashMap<>());
                }

                Map<String, Object> existingTopics = progressToSave.getCompletedSubtopics();
                Map<String, Object> incomingTopics = newProgress.getCompletedSubtopics();

             // Define the prefix for the current save operation
                String prefix = course + "_" + standard + "_";

                for (Map.Entry<String, Object> topicEntry : incomingTopics.entrySet()) {
                    String rawKey = topicEntry.getKey();

                    // Skip metadata keys
                    if (rawKey.equalsIgnoreCase("course") || rawKey.equalsIgnoreCase("standard")) continue;

                    // ✅ CRITICAL FIX:
                    // If the key does NOT match the current course, skip it completely.
                    if (!rawKey.toLowerCase().startsWith(prefix.toLowerCase())) {
                        continue; // Do not process keys from other courses
                    }
                    
                    // If we are here, the key is correct.
                    String topicKey = rawKey; 

                    Object incomingSubtopicsObj = topicEntry.getValue();

                    @SuppressWarnings("unchecked")
                    Map<String, Boolean> existingSubtopics =
                            (Map<String, Boolean>) existingTopics.getOrDefault(topicKey, new HashMap<String, Boolean>());

                    if (incomingSubtopicsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Boolean> incomingSubtopics = (Map<String, Boolean>) incomingSubtopicsObj;
                        existingSubtopics.putAll(incomingSubtopics);
                    } else {
                        existingTopics.put(topicKey, incomingSubtopicsObj);
                        continue;
                    }

                    existingTopics.put(topicKey, existingSubtopics);
                }
            }

            // --- MERGE LOGIC FOR SUBJECT COMPLETION ---
            if (newProgress.getSubjectCompletion() != null) {
                if (progressToSave.getSubjectCompletion() == null) {
                    progressToSave.setSubjectCompletion(new HashMap<>());
                }

                Map<String, Integer> existing = progressToSave.getSubjectCompletion();
                // Use the same prefix as defined above
                String subjectPrefix = course + "_" + standard + "_"; 

                for (Map.Entry<String, Integer> entry : newProgress.getSubjectCompletion().entrySet()) {
                    String rawKey = entry.getKey();
                    
                    // ✅ CRITICAL FIX:
                    // If the key does NOT match the current course, skip it.
                    if (!rawKey.toLowerCase().startsWith(subjectPrefix.toLowerCase())) {
                        continue;
                    }

                    // Key is correct, put it in
                    existing.put(rawKey, entry.getValue());
                }
            }

            // 3️⃣ Save merged document
            Progress saved = progressRepository.save(progressToSave);
            System.out.println("✅ Saved progress for user: " + saved.getUserId() + " [" + course + " - " + standard + "]");
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error during progress save: " + e.getMessage()));
        }
    }

    
    
    
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProgress(
        @RequestParam String userId,
        @RequestParam(required = false) String course,
        @RequestParam(required = false) String standard,
        @RequestParam(required = false) String subject) {
      try {
        Progress progress = progressRepository.findByUserId(userId);
        if (progress != null) {
          if (course != null && standard != null && subject != null) {
            // ✅ Remove only that specific subject (e.g. NEET_11th_Chemistry_)
            String prefix = (course + "_" + standard + "_" + subject + "_").toLowerCase();

            Map<String, Object> completed = progress.getCompletedSubtopics();
            if (completed != null) {
              completed.entrySet().removeIf(entry ->
                entry.getKey().toLowerCase().startsWith(prefix)
              );
            }

            Map<String, Integer> subjectMap = progress.getSubjectCompletion();
            if (subjectMap != null) {
              subjectMap.entrySet().removeIf(entry ->
                entry.getKey().toLowerCase().startsWith(course.toLowerCase() + "_" + standard.toLowerCase() + "_" + subject.toLowerCase())
              );
            }

            progressRepository.save(progress);
            System.out.println("🗑️ Cleared progress for " + subject + " (" + course + " - " + standard + ")");
          } else if (course != null && standard != null) {
            // fallback — remove all NEET 11th if no subject passed
            String prefix = (course + "_" + standard + "_").toLowerCase();
            Map<String, Object> completed = progress.getCompletedSubtopics();
            if (completed != null)
              completed.entrySet().removeIf(entry -> entry.getKey().toLowerCase().startsWith(prefix));

            Map<String, Integer> subjectMap = progress.getSubjectCompletion();
            if (subjectMap != null)
              subjectMap.entrySet().removeIf(entry -> entry.getKey().toLowerCase().startsWith(prefix));

            progressRepository.save(progress);
            System.out.println("🗑️ Cleared partial progress for " + course + " " + standard);
          } else {
            progressRepository.deleteAllByUserId(userId);
            System.out.println("🗑️ Deleted ALL progress for " + userId);
          }
        }
        return ResponseEntity.ok(Map.of("message", "Progress deleted successfully"));
      } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to delete progress: " + e.getMessage()));
      }
    }
 



}
