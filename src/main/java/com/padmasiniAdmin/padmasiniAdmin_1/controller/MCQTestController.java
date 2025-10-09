package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.Collections;

import com.padmasiniAdmin.padmasiniAdmin_1.service.MCQTestService;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;


import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.MCQTestService;

@RestController
public class MCQTestController {
	@Autowired
	MCQTestService mcqTestService;
	@PostMapping("/addQuestion/{parentId}")
	public ResponseEntity<?> addQuestion(
	        @PathVariable("parentId") String parentId,
	        @RequestBody WrapperMCQTest question) {
	    try {
	        // enforce parentId from URL
	        question.setParentId(parentId);

	        // check for rootId
	        if (question.getRootId() == null || question.getRootId().isBlank()) {
	            return ResponseEntity.badRequest().body("❌ rootId is required in request body");
	        }

	        String unitName = mcqTestService.addQuestion(question);
	        return ResponseEntity.ok(Collections.singletonMap("message", "✅ Question added to unit " + unitName));

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("❌ Server Error: " + e.getMessage());
	    }
	}



	@PutMapping("/updateQuestion/{parentId}/{oldName}")
	public ResponseEntity<?> updateQuestion(@PathVariable("parentId") String id,@PathVariable("oldName") String oldName,@RequestBody WrapperMCQTest question) {
		System.out.println("updated Received Question: " + question);
		if(question.getParentId().isEmpty())return ResponseEntity.badRequest().body("No Unit is selected");
		question.setParentId(id);
		String unitName=mcqTestService.updateQuestion(question,oldName);
		
		if(unitName.isEmpty())return ResponseEntity.badRequest().body("No Unit present under the provided Unit Name");
		return ResponseEntity.ok(Collections.singletonMap("message", "Question added to the unit"));	}
	@DeleteMapping("/deleteQuestion/{parentId}")
	public ResponseEntity<?> deleteQuestion(@PathVariable("parentId") String id,@RequestBody WrapperMCQTest question) {
		System.out.println("inside delete: ");
		if(question.getParentId().isEmpty())return ResponseEntity.badRequest().body("No Unit is selected");
		question.setParentId(id);
		String unitName=mcqTestService.deleteQuestion(question);
		if(!unitName.isEmpty())System.out.println("deleted successfully");
		if(unitName.isEmpty())return ResponseEntity.badRequest().body("No Unit is present under the provided Unit Name");
		return ResponseEntity.ok(Collections.singletonMap("message", "Question added to the unit"));		//System.out.println("Received Question: " + question.getQuestion());
	}
}