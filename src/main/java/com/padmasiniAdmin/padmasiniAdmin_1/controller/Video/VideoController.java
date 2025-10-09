package com.padmasiniAdmin.padmasiniAdmin_1.controller.Video;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class VideoController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/addVideo")
    public ResponseEntity<?> addVideo(@RequestBody Map<String, String> body) {
        String subtopicId = body.get("subtopicId");
        String videoUrl = body.get("videoUrl");

        if (subtopicId == null || videoUrl == null) {
            return ResponseEntity.badRequest().body("Missing subtopicId or videoUrl");
        }

        try {
            Query query = new Query(Criteria.where("_id").is(new ObjectId(subtopicId)));
            Update update = new Update().set("video_url", videoUrl);
            mongoTemplate.updateFirst(query, update, "subtopics"); // 👈 collection name

            return ResponseEntity.ok(Map.of("status", "ok", "videoUrl", videoUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}