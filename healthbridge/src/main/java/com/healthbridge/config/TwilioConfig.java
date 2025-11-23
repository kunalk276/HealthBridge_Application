package com.healthbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.twilio.Twilio;

import jakarta.annotation.PostConstruct;

@Configuration
public class TwilioConfig {

	@Value("${twilio.account_sid}")
	private String accountSid;

	@Value("${twilio.auth_token}")
	private String authToken;

	@PostConstruct
	public void initTwilio() {
		Twilio.init(accountSid, authToken);
	}
}
