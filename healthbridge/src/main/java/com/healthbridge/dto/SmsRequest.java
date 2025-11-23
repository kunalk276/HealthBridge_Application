package com.healthbridge.dto;


public class SmsRequest {

	private String phone;
	private String userName;
	private String hospitalName;
	private String hospitalContact;
	private String googleMapLink;
	private String severity;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHospitalName() {
		return hospitalName;
	}

	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}

	public String getHospitalContact() {
		return hospitalContact;
	}

	public void setHospitalContact(String hospitalContact) {
		this.hospitalContact = hospitalContact;
	}

	public String getGoogleMapLink() {
		return googleMapLink;
	}

	public void setGoogleMapLink(String googleMapLink) {
		this.googleMapLink = googleMapLink;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
}
