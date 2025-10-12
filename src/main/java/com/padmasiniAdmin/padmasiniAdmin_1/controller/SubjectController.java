package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.List;
import java.util.ArrayList;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Subject;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.SubjectRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestParam;


	
@RestController
public class SubjectController {
	@Autowired
	SubjectRepository repo;
	@Autowired
	private MongoTemplate mongoTemplate;

	@PostMapping("/addSubject")
	public ResponseEntity<?> addSubject(@RequestBody Subject subject) {
		String name=subject.getName().trim().toLowerCase();
		if(name.isEmpty()) {
			return ResponseEntity.badRequest().body("please enter a subject name");
		}
		Subject dupSubject=repo.findByName(name);
		if(dupSubject.getName().equals(name)) {
			return ResponseEntity.badRequest().body("Subject name already exist");
		}
		subject.setName(name);
		repo.save(subject);
		return ResponseEntity.ok("subject added successfully");
		//System.out.println("subject added successfully");
	}
	
	
	@GetMapping("/getAllSubjects")
	public List<Subject> fetchSubject(HttpServletRequest req) {
		System.out.println("subject fetched succeddfully");
		Cookie[]cookie=req.getCookies();
		if(cookie!=null)for(Cookie c:cookie) {
			System.out.println(c);
		}
		List<Subject> subject= repo.findAll();
		for(Subject sub:subject) {
			String formattedName = sub.getName().substring(0, 1).toUpperCase() + sub.getName().substring(1).toLowerCase();
            sub.setName(formattedName);
		}
		return subject;
	}
	
	@PutMapping("/updateSubject/{name}")
	public ResponseEntity<?> update (@RequestBody Subject subject,@PathVariable String subjectName) {
		String name=subjectName.trim().toLowerCase();
		if(name.isEmpty()) {
			return ResponseEntity.badRequest().body("please enter a subject name");
		}
		if(subject==null) {
			return ResponseEntity.badRequest().body("please select a subject");
		}
		 Subject existing = repo.findByName(name);
		 System.out.println(existing.getName());
		    if (existing != null) {
		        existing.setName(subject.getName().toLowerCase()); // or update other fields
		        repo.save(existing);
		        return ResponseEntity.ok("Subject updated successfully");
		        //System.out.println("subject updated successfully");
		    }
		    else return ResponseEntity.badRequest().body("no such subject exist");
	}
	
	@DeleteMapping("/deleteSubject/{name}")
	public ResponseEntity<?> delete(@PathVariable String subjectName) {
		String name=subjectName.trim().toLowerCase();
		if(name.isEmpty()) {
			return ResponseEntity.badRequest().body("please enter a subject name");
		}
		long del=repo.deleteByName(name);
		mongoTemplate.dropCollection(name);
		if(del==0)return ResponseEntity.badRequest().body("no such subject exist");
		else return ResponseEntity.ok("subject and its data deleted successfully");
	}
	
	@GetMapping("/getSubjectDetails")
	public ResponseEntity<?> getSubjectDetails(
	        @RequestParam String courseName,
	        @RequestParam String subjectName,
	        @RequestParam(required = false) String standard) {
	    try {
	        // Correct collection name: courseName + "." + subjectName
	        String collectionName = courseName + "." + subjectName;
	        System.out.println("📘 Fetching from MongoDB collection: " + collectionName);

	        // Build query for standard if given
	        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();

	        if (standard != null && !standard.isEmpty()) {
	            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("standard").is(standard));
	        }

	        // Fetch from the correct collection (e.g. professional.Physics)
	        List<?> data = mongoTemplate.find(query, Object.class, collectionName);

	        System.out.println("✅ Found " + data.size() + " records in " + collectionName);

	        return ResponseEntity.ok(data);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.internalServerError().body("Error fetching subject data");
	    }
	}



}
