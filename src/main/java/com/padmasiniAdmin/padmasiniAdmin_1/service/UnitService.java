package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class UnitService {

    @Autowired
    private MongoClient mongoClient;

    private final Region region = Region.AP_SOUTH_1;
    private final String bucketName = "trilokinnovations-test-admin";

    // =============================
    // 🔹 Mongo Helper
    // =============================
    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    private boolean headUnitExist(String dbName, String name, String collectionName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Query query = new Query(Criteria.where("unitName").is(name));
        return mongoTemplate.exists(query, UnitRequest.class, collectionName);
    }

    // =============================
    // 🔹 Head Unit CRUD
    // =============================
    public boolean addNewHeadUnit(WrapperUnitRequest request) {
        if (!headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = getTemplate(request.getDbname());
            mongoTemplate.save(request.getUnit(), request.getSubjectName());
            System.out.println("✅ Head unit saved");
            return true;
        }
        System.out.println("⚠️ Head unit already exists");
        return false;
    }

    public boolean deleteHeadUnit(WrapperUnitRequest request) {
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = getTemplate(request.getDbname());
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            mongoTemplate.remove(query, UnitRequest.class, request.getSubjectName());
            System.out.println("🗑️ Head unit deleted");
            return true;
        }
        System.out.println("⚠️ Head unit not found");
        return false;
    }

    public boolean updateHeadUnitName(WrapperUnitRequest request, String newUnitName) {
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = getTemplate(request.getDbname());
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            Update update = new Update().set("unitName", newUnitName);
            mongoTemplate.updateFirst(query, update, UnitRequest.class, request.getSubjectName());
            System.out.println("✏️ Head unit renamed");
            return true;
        }
        System.out.println("❌ Head unit not found");
        return false;
    }

    public List<UnitRequest> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Query query = new Query(Criteria.where("standard").is(standard));
        return mongoTemplate.find(query, UnitRequest.class, subjectName);
    }

    // =============================
    // 🔹 Add Unit (return inserted subunit ID)
    // =============================
    public String addUnit(WrapperUnit data) {
        System.out.println("📥 Received addUnit request");
        System.out.println("🧩 RootUnitId: " + data.getRootUnitId());
        System.out.println("🧩 ParentId: " + data.getParentId());
        System.out.println("🧩 UnitName: " + data.getUnitName());

        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return null;
        }

        boolean assignTest = data.getParentId().equals(data.getRootUnitId());
        Unit unit = new Unit(assignTest);
        unit.setId(new ObjectId().toString()); // assign new ID
        unit.setParentId(data.getParentId());
        unit.setUnitName(data.getUnitName());
        unit.setExplanation(data.getExplanation());
        unit.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
        unit.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());

        System.out.println("✅ Processed imageUrls for saving: " + unit.getImageUrls());

        boolean inserted = false;

        if (root.getId().equals(data.getParentId())) {
            root.getUnits().add(unit);
            inserted = true;
        } else if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (insertIntoParent(u, data.getParentId(), unit)) {
                    inserted = true;
                    break;
                }
            }
        }

        if (inserted) {
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit added successfully: " + unit.getUnitName());
            return unit.getId(); // return inserted ID
        } else {
            System.out.println("⚠️ Parent ID not found");
            return null;
        }
    }

    // =============================
    // 🔹 Update Unit
    // =============================
    public boolean updateUnit(WrapperUnit data) {
        System.out.println("✏️ Received updateUnit request for: " + data.getUnitName());

        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return false;
        }

        boolean updated = false;

        // If updating root itself
        if (root.getId().equals(data.getParentId())) {
            root.setUnitName(data.getUnitName());
            root.setExplanation(data.getExplanation());
            root.setImageUrls(data.getImageUrls());
            root.setAudioFileId(data.getAudioFileId());
            updated = true;
        } else if (root.getUnits() != null) {
            for (Unit unit : root.getUnits()) {
                if (updateParent(unit, data.getParentId(), data)) {
                    updated = true;
                    break;
                }
            }
        }

        if (updated) {
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit updated successfully with new data");
            return true;
        } else {
            System.out.println("⚠️ Parent ID not found");
            return false;
        }
    }

    // =============================
    // 🔹 Delete Unit
    // =============================
    public boolean deleteUnit(WrapperUnit data) {
        System.out.println("🗑️ Deleting unit with ID: " + data.getParentId());

        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return false;
        }

        boolean deleted = false;

        // If deleting root itself
        if (root.getId().equals(data.getParentId())) {
            if (root.getUnits() != null) {
                for (Unit u : root.getUnits()) {
                    deleteAllFiles(u);
                }
            }
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(root.getId())), UnitRequest.class, data.getSubjectName());
            System.out.println("🗑️ Root unit deleted");
            return true;
        }

        // Try deleting from direct children
        if (root.getUnits() != null) {
            deleted = removeUnitById(root.getUnits(), data.getParentId());
        }

        // Try deleting from nested subunits
        if (!deleted && root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (deleteFromSubUnits(u, data.getParentId())) {
                    deleted = true;
                    break;
                }
            }
        }

        if (deleted) {
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit deleted successfully");
            return true;
        } else {
            System.out.println("⚠️ Parent ID not found");
            return false;
        }
    }


    // =============================
    // 🔹 Recursive Helpers
    // =============================
    private boolean insertIntoParent(Unit current, String targetParentId, Unit newUnit) {
        if (targetParentId.equals(current.getId())) {
            current.getUnits().add(newUnit);
            return true;
        }
        if (current.getUnits() != null) {
            for (Unit child : current.getUnits()) {
                if (insertIntoParent(child, targetParentId, newUnit)) return true;
            }
        }
        return false;
    }

    private boolean updateParent(Unit current, String targetParentId, WrapperUnit data) {
        if (targetParentId.equals(current.getId())) {
            current.setUnitName(data.getUnitName());
            current.setExplanation(data.getExplanation());
            current.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
            current.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());
            return true;
        }

        if (current.getUnits() != null) {
            for (Unit child : current.getUnits()) {
                if (updateParent(child, targetParentId, data)) return true;
            }
        }
        return false;
    }

    private boolean deleteFromSubUnits(Unit current, String targetId) {
        if (current.getUnits() != null) {
            boolean removed = removeUnitById(current.getUnits(), targetId);
            if (removed) return true;

            for (Unit child : current.getUnits()) {
                if (deleteFromSubUnits(child, targetId)) return true;
            }
        }
        return false;
    }

    private boolean removeUnitById(List<Unit> units, String targetId) {
        Iterator<Unit> iterator = units.iterator();
        while (iterator.hasNext()) {
            Unit unit = iterator.next();
            if (targetId.equals(unit.getId())) {
                deleteAllFiles(unit);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // =============================
    // 🔹 File Management (S3)
    // =============================
    private void deleteFileFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) return;

        String fileKey = fileUrl.split(".amazonaws.com/")[1];
        try (S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
        } catch (Exception e) {
            System.err.println("❌ S3 delete failed: " + e.getMessage());
        }
    }

    private void deleteAllFiles(Unit unit) {
        if (unit.getAudioFileId() != null) {
            unit.getAudioFileId().forEach(this::deleteFileFromS3);
        }
        if (unit.getImageUrls() != null) {
            unit.getImageUrls().forEach(this::deleteFileFromS3);
        }
        if (unit.getUnits() != null) {
            unit.getUnits().forEach(this::deleteAllFiles);
        }
    }

    // =============================
    // 🔹 Utility
    // =============================
    public UnitRequest getById(String id, String collectionName, String dbName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
}
