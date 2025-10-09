package com.padmasiniAdmin.padmasiniAdmin_1.model;

public class WrapperUnitRequest {
private String dbname;
private UnitRequest unit;
private String subjectName;

public String getSubjectName() {
	return subjectName;
}
public void setSubjectName(String subjectName) {
	this.subjectName = subjectName;
}
public String getDbname() {
	return dbname;
}
public void setDbname(String dbname) {
	this.dbname = dbname;
}
public UnitRequest getUnit() {
	return unit;
}
public void setUnit(UnitRequest unit) {
	this.unit = unit;
}
@Override
public String toString() {
	return "WrapperUnitRequest [dbname=" + dbname + ", unit=" + unit + "]";
}

}
