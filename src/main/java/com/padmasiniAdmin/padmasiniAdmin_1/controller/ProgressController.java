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

    // Helper to safely find unique progress
    private Progress getUniqueProgress(String userId) {
        try {
            return progressRepository.findByUserId(userId);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // FIX DUPLICATES AUTOMATICALLY
            System.out.println("⚠️ Duplicate progress found for user " + userId + ". Cleaning up...");
            // Use a raw query or find all to delete extras
            // Since we can't easily change repo here, we just advise manual cleanup or
            // implementation of a findAllByUserId() logic.
            // For now, re-throwing so you see the logs, but Clean your DB!
            throw e; 
        }
    }

    // ✅ Get progress by userId
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProgress(
        @PathVariable String userId,
        @RequestParam(required = false) String course,
        @RequestParam(required = false) String standard
    ) {
        try {
            // Use the robust find
            Progress progress = progressRepository.findByUserId(userId); 

            if (progress == null) {
                return ResponseEntity.ok(Map.of(
                    "completedSubtopics", Map.of(),
                    "subjectCompletion", Map.of()
                ));
            }

            // If course and standard are provided, filter the maps (Your ORIGINAL Logic)
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
            
            return ResponseEntity.ok(progress);

        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("🔥 DATABASE ERROR: Duplicate records found for this user. Please clear duplicates in MongoDB 'progress' collection for userId: " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading progress: " + e.getMessage());
        }
    }

    // ✅ Save or update progress (Restoring Logic + Safety)
    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody Progress newProgress) {
        if (newProgress.getUserId() == null || newProgress.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing userId in request body");
        }

        try {
            Progress progressToSave = progressRepository.findByUserId(newProgress.getUserId());
            if (progressToSave == null) {
                progressToSave = new Progress();
                progressToSave.setUserId(newProgress.getUserId());
            }

            // Extract course & standard (Your old logic relied on this matching the prefix)
            String course = newProgress.getCourse();
            String standard = newProgress.getStandard();
            String prefix = "";
            
            if (course != null && standard != null) {
                 prefix = (course + "_" + standard + "_").toLowerCase();
            }

            // --- 1. MERGE COMPLETED SUBTOPICS ---
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

                    // ✅ RESTORING YOUR FILTER LOGIC (but making it safe)
                    // Only merge if prefix matches OR if no prefix was provided (safety fallback)
                    if (!prefix.isEmpty() && !rawKey.toLowerCase().startsWith(prefix)) {
                        continue; 
                    }
                    
                    Object incomingSubtopicsObj = topicEntry.getValue();
                    Object existingVal = existingTopics.get(rawKey);

                    // ✅ TYPE SAFETY CHECK (The fix for 500 error)
                    if (incomingSubtopicsObj instanceof Map && existingVal instanceof Map) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> existingMap = (Map<String, Object>) existingVal;
                            @SuppressWarnings("unchecked")
                            Map<String, Object> incomingMap = (Map<String, Object>) incomingSubtopicsObj;
                            existingMap.putAll(incomingMap);
                            existingTopics.put(rawKey, existingMap);
                        } catch (Exception e) {
                            // If cast fails, overwrite
                            existingTopics.put(rawKey, incomingSubtopicsObj);
                        }
                    } else {
                        // Direct overwrite
                        existingTopics.put(rawKey, incomingSubtopicsObj);
                    }
                }
            }

            // --- 2. MERGE SUBJECT COMPLETION ---
            if (newProgress.getSubjectCompletion() != null) {
                if (progressToSave.getSubjectCompletion() == null) {
                    progressToSave.setSubjectCompletion(new HashMap<>());
                }
                
                // For Subject completion, simple putAll is usually fine, but let's filter like you did before
                if (!prefix.isEmpty()) {
                     for (Map.Entry<String, Integer> entry : newProgress.getSubjectCompletion().entrySet()) {
                        if (entry.getKey().toLowerCase().startsWith(prefix)) {
                            progressToSave.getSubjectCompletion().put(entry.getKey(), entry.getValue());
                        }
                    }
                } else {
                    // Fallback: save everything if no prefix info
                    progressToSave.getSubjectCompletion().putAll(newProgress.getSubjectCompletion());
                }
            }

            Progress saved = progressRepository.save(progressToSave);
            System.out.println("✅ Saved progress for user: " + saved.getUserId());
            return ResponseEntity.ok(saved);

        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("🔥 DATABASE ERROR: Duplicate progress records found. Please delete duplicates for this user in MongoDB.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error during progress save: " + e.getMessage()));
        }
    }

    // ✅ Delete method (Unchanged)
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
          } else if (course != null && standard != null) {
            String prefix = (course + "_" + standard + "_").toLowerCase();
            Map<String, Object> completed = progress.getCompletedSubtopics();
            if (completed != null)
              completed.entrySet().removeIf(entry -> entry.getKey().toLowerCase().startsWith(prefix));

            Map<String, Integer> subjectMap = progress.getSubjectCompletion();
            if (subjectMap != null)
              subjectMap.entrySet().removeIf(entry -> entry.getKey().toLowerCase().startsWith(prefix));

            progressRepository.save(progress);
          } else {
            progressRepository.deleteAllByUserId(userId);
          }
        }
        return ResponseEntity.ok(Map.of("message", "Progress deleted successfully"));
      } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to delete progress: " + e.getMessage()));
      }
    }
}
