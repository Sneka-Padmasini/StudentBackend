package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "studentUserDetail") 
public class UserModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	// Core fields
	private String firstname; 
	private String lastname;
	private String email;
	private String password;
	private String mobile; 
	private String role;
	private String coursetype;
	private String courseName;
	private List<String> standards = new ArrayList<>();
	private List<String> subjects = new ArrayList<>();

	// DB fields for selected courses / standards
	private Map<String, List<String>> selectedCourse;
	private List<String> selectedStandard;

	// Optional fields
	private String photo;
	private String dob;
	private String gender;
	private Boolean isVerified;

	// 🔥 NEW FIELDS FOR SUBSCRIPTION 🔥
	private String plan;
	private String startDate;
	private String endDate;
	
	private String paymentId;      // e.g., "pay_N9s8d7f6g5h4j"
    private String paymentMethod;  // e.g., "Razorpay" or "Free Trial"
    private String amountPaid;     // e.g., "1000" or "0"
    
    private String payerId; // Stores UPI ID or Payer Email
    
    private int comfortableDailyHours;

	// ---------------- Getters and Setters ----------------
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getFirstname() { return firstname; }
	public void setFirstname(String firstname) { this.firstname = firstname; }

	public String getLastname() { return lastname; }
	public void setLastname(String lastname) { this.lastname = lastname; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getMobile() { return mobile; }
	public void setMobile(String mobile) { this.mobile = mobile; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

	public String getCoursetype() { return coursetype; }
	public void setCoursetype(String coursetype) { this.coursetype = coursetype; }

	public String getCourseName() { return courseName; }
	public void setCourseName(String courseName) { this.courseName = courseName; }

	public List<String> getStandards() { return standards; }
	public void setStandards(List<String> standards) { this.standards = standards; }

	public List<String> getSubjects() { return subjects; }
	public void setSubjects(List<String> subjects) { this.subjects = subjects; }

	public Map<String, List<String>> getSelectedCourse() { return selectedCourse; }
	public void setSelectedCourse(Map<String, List<String>> selectedCourse) { this.selectedCourse = selectedCourse; }

	public List<String> getSelectedStandard() { return selectedStandard; }
	public void setSelectedStandard(List<String> selectedStandard) { this.selectedStandard = selectedStandard; }

	public String getPhoto() { return photo; }
	public void setPhoto(String photo) { this.photo = photo; }

	public String getDob() { return dob; }
	public void setDob(String dob) { this.dob = dob; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public Boolean getIsVerified() { return isVerified; }
	public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

	// 🔥 GETTERS AND SETTERS FOR NEW FIELDS 🔥
	public String getPlan() { return plan; }
	public void setPlan(String plan) { this.plan = plan; }

	public String getStartDate() { return startDate; }
	public void setStartDate(String startDate) { this.startDate = startDate; }

	public String getEndDate() { return endDate; }
	public void setEndDate(String endDate) { this.endDate = endDate; }
	
	// 🔥 ADD GETTERS AND SETTERS FOR NEW FIELDS 🔥
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAmountPaid() { return amountPaid; }
    public void setAmountPaid(String amountPaid) { this.amountPaid = amountPaid; }
    
    public String getPayerId() { return payerId; }
    public void setPayerId(String payerId) { this.payerId = payerId; }
    
    public int getComfortableDailyHours() { return comfortableDailyHours; }
    public void setComfortableDailyHours(int comfortableDailyHours) { this.comfortableDailyHours = comfortableDailyHours; }

	// Compatibility getter for frontend userName
	public String getUserName() {
		String f = (firstname != null) ? firstname : "";
		String l = (lastname != null) ? lastname : "";
		return f + " " + l;
	}

	@Override
	public String toString() {
		return "UserModel [id=" + id + ", firstname=" + firstname + ", email=" + email + ", plan=" + plan + ", endDate=" + endDate + "]";
	}
}
