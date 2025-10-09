package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;

@RestController
@RequestMapping("/api")
public class UnitController {

    @Autowired
    private UnitService unitService;

    // -----------------------------
    // Get all units for a course/subject/standard
    // -----------------------------
    @GetMapping("/getAllUnits/{dbname}/{subjectName}/{standard}")
    public List<UnitRequest> getUnitsBySubject(@PathVariable String dbname,
                                               @PathVariable String subjectName,
                                               @PathVariable String standard) {
        List<UnitRequest> units = unitService.getAllUnit(dbname, subjectName, standard);
        return units != null ? units : new ArrayList<>();
    }

    // -----------------------------
    // Add new head unit
    // -----------------------------
    @PostMapping("/addNewHeadUnit")
    public ResponseEntity<Map<String, String>> addHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.addNewHeadUnit(request) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Add new subtopic/unit
    // -----------------------------
    @PostMapping("/addSubtopic")
    public ResponseEntity<Map<String, Object>> addSubtopic(@RequestBody WrapperUnit unit) {
        String insertedSubId = unitService.addUnit(unit);

        Map<String, Object> response = new HashMap<>();
        response.put("status", insertedSubId != null ? "success" : "failed");
        response.put("insertedSubId", insertedSubId);

        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Update head unit
    // -----------------------------
    @PutMapping("/updateHeadUnit/{newUnitName}")
    public ResponseEntity<Map<String, String>> updateHeadUnit(@RequestBody WrapperUnitRequest request,
                                                              @PathVariable String newUnitName) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.updateHeadUnitName(request, newUnitName) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Delete head unit
    // -----------------------------
    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.deleteHeadUnit(request) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

 // -----------------------------
 // Delete a subunit
 // -----------------------------
 @DeleteMapping("/deleteUnit")
 public ResponseEntity<Map<String, String>> deleteUnit(@RequestBody WrapperUnit unit) {
     boolean deleted = unitService.deleteUnit(unit);
     Map<String, String> response = new HashMap<>();
     response.put("status", deleted ? "deleted" : "failed");
     return ResponseEntity.ok(response);
 }

 // -----------------------------
 // Update a subunit
 // -----------------------------
 @PutMapping("/updateSubsection")
 public ResponseEntity<Map<String, String>> updateSubUnit(@RequestBody WrapperUnit unit) {
     boolean updated = unitService.updateUnit(unit);
     Map<String, String> response = new HashMap<>();
     response.put("status", updated ? "updated" : "failed");
     return ResponseEntity.ok(response);
 }

    // -----------------------------
    // Add subunit (alternate endpoint)
    // -----------------------------
    @PostMapping("/addNewSubsection")
    public ResponseEntity<Map<String, Object>> addSubUnit(@RequestBody WrapperUnit unit) {
        String insertedSubId = unitService.addUnit(unit);

        Map<String, Object> response = new HashMap<>();
        response.put("status", insertedSubId != null ? "success" : "failed");
        response.put("insertedSubId", insertedSubId);

        return ResponseEntity.ok(response);
    }
}
