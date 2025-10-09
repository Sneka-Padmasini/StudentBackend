package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Subject;

public interface SubjectRepository extends MongoRepository<Subject, Integer> {
	
	Subject findByName(String name);

	long deleteByName(String name);
}
