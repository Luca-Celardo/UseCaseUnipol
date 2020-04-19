package com.example.magnewsdequeuermail.services;

import com.example.magnewsdequeuermail.model.Email;
import com.example.magnewsdequeuermail.model.EmailOutcome;
import org.json.JSONObject;

import javax.mail.MessagingException;

public interface EmailDequeuer {

    Email readEmailRequestFromEmailQueue(JSONObject emailRequest);
    EmailOutcome sendEmail(Email email) throws MessagingException;
}
