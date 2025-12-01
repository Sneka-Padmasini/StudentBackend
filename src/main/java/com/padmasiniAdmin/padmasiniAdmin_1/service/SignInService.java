package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.util.regex.Pattern;
import org.bson.Document;
import java.util.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;

@Service
public class SignInService {

    @Autowired
    private MongoClient mongoClient;

    private final String dbName = "studentUsers";
    private final String collectionName = "studentUserDetail";


    public UserModel checkUserEmail(String email, String password) {
        MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);
        Query query = new Query();
        query.addCriteria(Criteria.where("email").regex("^" + Pattern.quote(email) + "$", "i"));

        Document doc = mongo.getCollection(collectionName).find(query.getQueryObject()).first();

        System.out.println("Login attempt with email: " + email);
        if (doc == null) {
            System.out.println("No user found in DB");
            return null;
        }

        // ✅ Add this line to include MongoDB ObjectId
        String objectId = doc.getObjectId("_id").toHexString();

        // Manual mapping
        UserModel user = new UserModel();
        user.setId(objectId); // ✅ <-- this is the missing link!
        user.setFirstname(doc.getString("firstname"));
        user.setLastname(doc.getString("lastname"));
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        user.setMobile(doc.getString("mobile"));
        user.setRole(doc.getString("role"));
        user.setCoursetype(doc.getString("coursetype"));
        user.setCourseName(doc.getString("courseName"));
        user.setPhoto(doc.getString("photo"));
        user.setDob(doc.getString("dob"));
        user.setGender(doc.getString("gender"));
        user.setIsVerified(doc.getBoolean("isVerified", false));
        user.setPlan(doc.getString("plan"));
        user.setStartDate(doc.getString("startDate"));
        user.setEndDate(doc.getString("endDate"));
        user.setPaymentId(doc.getString("paymentId"));
        user.setPaymentMethod(doc.getString("paymentMethod"));
        user.setAmountPaid(doc.getString("amountPaid"));

        // ✅ Map selectedCourse
        Object selectedCourseObj = doc.get("selectedCourse");
        if (selectedCourseObj instanceof Document) {
            Map<String, List<String>> selectedCourseMap = new HashMap<>();
            Document selectedCourseDoc = (Document) selectedCourseObj;
            for (String key : selectedCourseDoc.keySet()) {
                Object val = selectedCourseDoc.get(key);
                if (val instanceof List<?>) {
                    selectedCourseMap.put(key, (List<String>) val);
                }
            }
            user.setSelectedCourse(selectedCourseMap);
        }

        // ✅ Map selectedStandard
        Object selectedStandardObj = doc.get("selectedStandard");
        if (selectedStandardObj instanceof List<?>) {
            user.setSelectedStandard((List<String>) selectedStandardObj);
        }

        // ✅ Flatten all standards across courses
        if (user.getSelectedCourse() != null) {
            List<String> allStandards = user.getSelectedCourse().values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            user.setStandards(allStandards);
        } else if (user.getSelectedStandard() != null) {
            user.setStandards(user.getSelectedStandard());
        }

        System.out.println("User found: " + user);

        if (user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }


    
    public void printAllUsers() {
        MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);
        var allUsers = mongo.findAll(UserModel.class, collectionName);
        System.out.println("All users in studentUserDetail collection:");
        allUsers.forEach(System.out::println);
    }
    
 // ================= CHECK IF EMAIL EXISTS =================
    public UserModel checkEmailExists(String email) {
        MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);

        Query query = new Query();
        query.addCriteria(Criteria.where("email").regex("^" + Pattern.quote(email) + "$", "i"));

        Document doc = mongo.getCollection(collectionName)
                .find(query.getQueryObject())
                .first();

        if (doc == null) return null;

        UserModel user = new UserModel();
        
        // Set only basic fields needed to verify user
        user.setId(doc.getObjectId("_id").toHexString());
        user.setEmail(doc.getString("email"));
        user.setFirstname(doc.getString("firstname"));
        user.setLastname(doc.getString("lastname"));

        return user;
    }

    public boolean updatePassword(String email, String newPassword) {
        try {
            MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);

            Query query = new Query();
            query.addCriteria(Criteria.where("email").is(email));

            Document update = new Document("$set", new Document("password", newPassword));

            mongo.getCollection(collectionName).updateOne(query.getQueryObject(), update);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    


    public boolean upgradeUserPlan(String email, UserModel updatedData) {
        try {
            MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);
            
            // 1. Find user by Email
            Query query = new Query();
            query.addCriteria(Criteria.where("email").is(email));

            // 2. Prepare the Update object (Use $set to protect other data)
            org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update();
            
            // Always update these on upgrade
            update.set("plan", updatedData.getPlan());
            update.set("startDate", updatedData.getStartDate());
            update.set("endDate", updatedData.getEndDate());
            update.set("paymentId", updatedData.getPaymentId());
            update.set("paymentMethod", updatedData.getPaymentMethod());
            update.set("amountPaid", updatedData.getAmountPaid());
            
            // Only update Courses/Standards if the user selected new ones
            if (updatedData.getSelectedCourse() != null && !updatedData.getSelectedCourse().isEmpty()) {
                update.set("selectedCourse", updatedData.getSelectedCourse());
                update.set("courseName", updatedData.getCourseName()); // Update the string version too
                update.set("coursetype", updatedData.getCoursetype());
            }
            
            if (updatedData.getSelectedStandard() != null && !updatedData.getSelectedStandard().isEmpty()) {
                update.set("selectedStandard", updatedData.getSelectedStandard());
                update.set("standards", updatedData.getStandards());
            }

            // 3. Execute Update
            mongo.getCollection(collectionName).updateOne(query.getQueryObject(), update.getUpdateObject());
            
            System.out.println("✅ User plan upgraded for: " + email);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
