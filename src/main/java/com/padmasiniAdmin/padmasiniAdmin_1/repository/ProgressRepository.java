// ProgressRepository.java

package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Progress;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProgressRepository extends MongoRepository<Progress, String> {
    
    // ✅ Change from List<Progress> to single Progress
    Progress findByUserId(String userId); 

    // ✅ This line is correct
    void deleteAllByUserId(String userId);
}