package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

public class UserDTO {
private String databaseName;
private String collectionName;
private UserModel user;
public String getDatabaseName() {
	return databaseName;
}
public void setDatabaseName(String databaseName) {
	this.databaseName = databaseName;
}
public String getCollectionName() {
	return collectionName;
}
public void setCollectionName(String collectionName) {
	this.collectionName = collectionName;
}
public UserModel getUser() {
	return user;
}
public void setUser(UserModel user) {
	this.user = user;
}
@Override
public String toString() {
	return "UserDTO [databaseName=" + databaseName + ", collectionName=" + collectionName + ", user=" + user + "]";
}

}
