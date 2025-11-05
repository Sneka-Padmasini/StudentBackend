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
        "http://localhost:5173",
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
    public ResponseEntity<?> getProgress(@PathVariable String userId) {
        try {
            // ❌ OLD LINE: List<Progress> progressList = progressRepository.findByUserId(userId);
            // ✅ NEW LINE:
            Progress progress = progressRepository.findByUserId(userId); // Returns single item or null

            // ❌ OLD LOGIC: if (progressList == null || progressList.isEmpty()) { ... }
            // ✅ NEW LOGIC:
            if (progress == null) { 
                return ResponseEntity.ok(Map.of(
                    "completedSubtopics", Map.of(),
                    "subjectCompletion", Map.of()
                ));
            }
            
            // ❌ OLD LOGIC: (Return the latest entry from the list)
            // Progress latest = progressList.get(progressList.size() - 1);
            // return ResponseEntity.ok(latest);

            // ✅ NEW LOGIC (Return the single result):
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

                for (Map.Entry<String, Object> topicEntry : incomingTopics.entrySet()) {
                    String rawKey = topicEntry.getKey();

                    // Skip metadata keys
                    if (rawKey.equalsIgnoreCase("course") || rawKey.equalsIgnoreCase("standard")) continue;

                    // Add prefix if missing
                    String topicKey = rawKey.startsWith(course + "_" + standard + "_")
                            ? rawKey
                            : course + "_" + standard + "_" + rawKey;

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
                for (Map.Entry<String, Integer> entry : newProgress.getSubjectCompletion().entrySet()) {
                    String rawKey = entry.getKey();
                    String prefixedKey = rawKey.startsWith(course + "_" + standard + "_")
                            ? rawKey
                            : course + "_" + standard + "_" + rawKey;
                    existing.put(prefixedKey, entry.getValue());
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


    
 // ProgressController.java (Inside the ProgressController class)

//    @DeleteMapping("/{userId}")
//    public ResponseEntity<?> deleteProgress(@PathVariable String userId) {
//        try {
//            // progressRepository.deleteAllByUserId is the correct method
//            progressRepository.deleteAllByUserId(userId);
//            System.out.println("🗑️ Deleted progress for user: " + userId);
//            // Returning a clean 200 OK with a simple message
//            return ResponseEntity.ok(Map.of("message", "Progress deleted successfully for " + userId));
//        } catch (Exception e) {
//            e.printStackTrace();
//            // Important: Return a 500 status with a clean JSON body for the frontend to read.
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to delete progress on the server: " + e.getMessage()));
//        }
//    }
    
//    @DeleteMapping("/{userId}")
//    public ResponseEntity<?> deleteProgress(
//        @PathVariable String userId,
//        @RequestParam(required = false) String course,
//        @RequestParam(required = false) String standard) {
//        
//        try {
//            Progress progress = progressRepository.findByUserId(userId);
//            if (progress != null) {
//                if (course != null && standard != null) {
//                    // Delete only specific course and standard progress
//                    String prefix = course + "_" + standard + "_";
//                    
//                    if (progress.getCompletedSubtopics() != null) {
//                        Map<String, Object> newCompletedSubtopics = new HashMap<>();
//                        progress.getCompletedSubtopics().forEach((key, value) -> {
//                            if (!key.startsWith(prefix)) {
//                                newCompletedSubtopics.put(key, value);
//                            }
//                        });
//                        progress.setCompletedSubtopics(newCompletedSubtopics);
//                    }
//                    
//                    if (progress.getSubjectCompletion() != null) {
//                        Map<String, Integer> newSubjectCompletion = new HashMap<>();
//                        progress.getSubjectCompletion().forEach((key, value) -> {
//                            if (!key.startsWith(prefix)) {
//                                newSubjectCompletion.put(key, value);
//                            }
//                        });
//                        progress.setSubjectCompletion(newSubjectCompletion);
//                    }
//                    
//                    progressRepository.save(progress);
//                    System.out.println("🗑️ Deleted progress for user: " + userId + " course: " + course + " standard: " + standard);
//                } else {
//                    // Delete all progress (existing behavior)
//                    progressRepository.deleteAllByUserId(userId);
//                    System.out.println("🗑️ Deleted ALL progress for user: " + userId);
//                }
//            }
//            
//            return ResponseEntity.ok(Map.of("message", "Progress deleted successfully"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", "Failed to delete progress on the server: " + e.getMessage()));
//        }
//    }


}
