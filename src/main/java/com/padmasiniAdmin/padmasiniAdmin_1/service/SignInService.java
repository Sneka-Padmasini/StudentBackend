package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.util.regex.Pattern;

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

        UserModel user = mongo.findOne(query, UserModel.class, collectionName);

        System.out.println("Login attempt with email: " + email);
        if(user == null) System.out.println("No user found in DB");
        else System.out.println("User found: " + user);

        if(user != null && user.getPassword().equals(password)) return user;

        return null;
    }

    public void printAllUsers() {
        MongoTemplate mongo = new MongoTemplate(mongoClient, dbName);
        var allUsers = mongo.findAll(UserModel.class, collectionName);
        System.out.println("All users in studentUserDetail collection:");
        allUsers.forEach(System.out::println);
    }
}
