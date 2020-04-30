package com.example.magnewsrestcontroller.controller;

import com.example.magnewsrestcontroller.model.Email;
import com.example.magnewsrestcontroller.model.EmailOutcome;
import com.example.magnewsrestcontroller.model.Outcome;
import com.example.magnewsrestcontroller.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class ApiController {
    private EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    public ApiController(EmailService emailService) {
        this.emailService = emailService;
    }

    @RequestMapping("/")
    public String Welcome(){
        return "Welcome to Email service!";
    }

    @PostMapping("/mail/request")
    public ResponseEntity<EmailOutcome> sendEmailRequest(@RequestBody Email email) {
        logger.info("Starting method");
        EmailOutcome emailOutcome = this.emailService.sendEmailRequest(email);
        if(emailOutcome.getOutcome() == Outcome.ACCEPTED) {
            logger.info("Method executed successfully!");
            return ResponseEntity.ok(emailOutcome);
        }
        else {
            logger.error("Method encountered errors!");
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/emailoutcomes/")
    public ResponseEntity<List<EmailOutcome>> getAllEmailOutcomes() {
        logger.info("Starting method");
        List<EmailOutcome> emailOutcomeList = this.emailService.getAllEmailOutcomes();
        return ResponseEntity.ok(emailOutcomeList);
    }

    @GetMapping("/emailoutcomes/{id}")
    public ResponseEntity<EmailOutcome> getEmailOutcomeById(@PathVariable int id) {
        logger.info("Starting method");
        EmailOutcome emailOutcome = this.emailService.getEmailOutcomeById(id);
        logger.info("Method executed successfully!");
        return ResponseEntity.ok(emailOutcome);
    }

    @GetMapping("/healthz")
    public String health() {
        return "OK";
    }
}
