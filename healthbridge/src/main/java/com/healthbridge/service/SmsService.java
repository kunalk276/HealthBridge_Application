package com.healthbridge.service;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.phone_number}")
    private String fromNumber;

    public String sendSms(String phone, String msg) {
        Message.creator(
                new PhoneNumber(phone),
                new PhoneNumber(fromNumber),
                msg
        ).create();

        return "SMS sent successfully";
    }
}
