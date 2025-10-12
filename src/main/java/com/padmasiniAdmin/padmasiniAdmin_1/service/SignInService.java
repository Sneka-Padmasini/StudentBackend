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

//    public UserModel checkUserEmail(String email, String password) {
//        MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);
//        Query query = new Query();
//        query.addCriteria(Criteria.where("email").regex("^" + Pattern.quote(email) + "$", "i"));
//
//        UserModel user = mongo.findOne(query, UserModel.class, collectionName);
//
//        System.out.println("Login attempt with email: " + email);
//        if(user == null) System.out.println("No user found in DB");
//        else System.out.println("User found: " + user);
//
//        if(user != null && user.getPassword().equals(password)) return user;
//
//        return null;
//    }

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

        // Manual mapping (important for nested objects)
        UserModel user = new UserModel();
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

        // ✅ Convert selectedCourse safely
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

        // ✅ Convert selectedStandard safely
        Object selectedStandardObj = doc.get("selectedStandard");
        if (selectedStandardObj instanceof List<?>) {
            user.setSelectedStandard((List<String>) selectedStandardObj);
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
}
