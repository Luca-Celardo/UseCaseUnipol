package com.example.magnewsrestcontroller.services;

import com.example.magnewsrestcontroller.model.Email;
import com.example.magnewsrestcontroller.model.EmailOutcome;

import java.util.List;

public interface EmailService {

    EmailOutcome sendEmailRequest(Email email);
    List<EmailOutcome> getAllEmailOutcomes();
    EmailOutcome getEmailOutcomeById(int id);
}
