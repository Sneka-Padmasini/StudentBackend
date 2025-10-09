package com.padmasiniAdmin.padmasiniAdmin_1.controller.student;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.service.studentService.StudentDataService;


@RestController
public class UnitDataController {
	
	@Autowired
	private StudentDataService data;
@PostMapping("/getUnitDataForStandrard/{standardType}")
public List<UnitRequest> getUnitDataForStudent(@PathVariable("standardType") String subject,@RequestBody WrapperUnit request){
	List<UnitRequest> unitData=data.getUnitData(subject,request);
	System.out.println(unitData);
	
	return unitData;
}
}
