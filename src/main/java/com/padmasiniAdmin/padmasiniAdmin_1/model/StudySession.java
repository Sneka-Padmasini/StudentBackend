package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "study_sessions")
public class StudySession {
    @Id
    private String id;
    private String userId; // Links to studentUserDetail _id
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private long durationInMinutes;

    public StudySession(String userId, LocalDateTime loginTime) {
        this.userId = userId;
        this.loginTime = loginTime;
    }

    // Standard Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    public long getDurationInMinutes() { return durationInMinutes; }
    public void setDurationInMinutes(long durationInMinutes) { this.durationInMinutes = durationInMinutes; }
}
