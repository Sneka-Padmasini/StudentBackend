package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.StudySession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface StudySessionRepository extends MongoRepository<StudySession, String> {
    // Find today's sessions for a specific user
    List<StudySession> findByUserIdAndLoginTimeBetween(String userId, LocalDateTime start, LocalDateTime end);
}
