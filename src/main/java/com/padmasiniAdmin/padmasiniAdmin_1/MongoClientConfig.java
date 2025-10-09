package com.padmasiniAdmin.padmasiniAdmin_1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoClientConfig {
	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;
	@Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}
