package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.aot.generate.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Subject {
	@Id
	private String id;
	private String name;
	




@Override
	public String toString() {
		return "Subject [id=" + id + ", name=" + name + "]";
	}

public Subject() {
		super();
	}

public Subject(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public String getId() {
	return id;
}

public void setId(String id) {
	this.id = id;
}

}
