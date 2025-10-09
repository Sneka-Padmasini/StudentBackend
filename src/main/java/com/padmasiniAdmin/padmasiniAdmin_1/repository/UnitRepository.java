package com.padmasiniAdmin.padmasiniAdmin_1.repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;

public interface UnitRepository extends MongoRepository<Unit, String> {
    // Optional: Add custom queries if needed
}

