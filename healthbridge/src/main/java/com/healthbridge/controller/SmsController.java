package com.healthbridge.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthbridge.dto.SmsRequest;
import com.healthbridge.service.SmsService;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "http://localhost:4200")
public class SmsController {
	@Autowired

	SmsService smsService;

	@PostMapping("/send")
	public String sendSms(@RequestBody SmsRequest request) {
		System.out.println("Sms request send");

		if (!"EMERGENCY".equalsIgnoreCase(request.getSeverity())) {
			return "Severity not EMERGENCY. SMS not sent.";
		}

		String message = "Hello " + request.getUserName() + ", your health severity is HIGH." + "\nHospital: "
				+ request.getHospitalName() + "\nContact: " + request.getHospitalContact() + "\nLocation: "
				+ request.getGoogleMapLink();

		return smsService.sendSms(request.getPhone(), message);
	}
}
