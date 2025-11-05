package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;
import java.util.HashMap;

@Document(collection = "Progress")
public class Progress {

    @Id
    private String id;
    private String userId;
    private Map<String, Object> completedSubtopics = new HashMap<>();
    private Map<String, Integer> subjectCompletion = new HashMap<>();

    // 🆕 ADD THESE TWO FIELDS
    private String course;
    private String standard;

    // ✅ Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getCompletedSubtopics() {
        return completedSubtopics;
    }

    public void setCompletedSubtopics(Map<String, Object> completedSubtopics) {
        this.completedSubtopics = completedSubtopics;
    }

    public Map<String, Integer> getSubjectCompletion() {
        return subjectCompletion;
    }

    public void setSubjectCompletion(Map<String, Integer> subjectCompletion) {
        this.subjectCompletion = subjectCompletion;
    }

    // 🆕 Add getters/setters for course and standard
    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }
}
