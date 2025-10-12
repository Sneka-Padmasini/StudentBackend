package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;

@Service
public class UserService {

	@Autowired
	private MongoClient mongoClient;

//	private final String dbName = "users";
	private final String dbName = "studentUsers";
	private final String collectionName = "studentUserDetail";

	public boolean saveNewUser(UserDTO user) {
		UserModel userModel = user.getUser();
		if (!checkUserEmail(userModel.getEmail())) {
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
			mongoTemplate.save(userModel, collectionName);
			System.out.println("User saved successfully in DB");
			return true;
		} else {
			System.out.println("User with email already exists: " + userModel.getEmail());
			return false;
		}
	}

	private boolean checkUserEmail(String email) {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
		Query query = new Query(Criteria.where("email").is(email));
		return mongoTemplate.exists(query, UserModel.class, collectionName);
	}

	public List<UserModel> getUsers() {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
		return mongoTemplate.findAll(UserModel.class, collectionName);
	}

	public void deleteUser(String email) {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
		Query query = new Query(Criteria.where("email").is(email));
		mongoTemplate.remove(query, UserModel.class, collectionName);
		System.out.println("User deleted: " + email);
	}
}
