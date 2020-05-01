package com.example.magnewsrestcontroller.services.impl;

import com.example.magnewsrestcontroller.model.Email;
import com.example.magnewsrestcontroller.model.EmailOutcome;
import com.example.magnewsrestcontroller.model.Outcome;
import com.example.magnewsrestcontroller.persistence.EmailOutcomePersister;
import com.example.magnewsrestcontroller.services.EmailService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private EmailOutcomePersister persister;
    private static final String topic = "magnews-topic";
    private static KafkaTemplate kafkaTemplate;

    @Autowired
    public EmailServiceImpl(EmailOutcomePersister persister, KafkaTemplate kafkaTemplate) {
        this.persister = persister;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public EmailOutcome sendEmailRequest(Email email) {
        logger.info("Trying to put the request for the email {} in the email queue", email);
        EmailOutcome emailOutcome = new EmailOutcome();
        emailOutcome.setOutcome(Outcome.NONE);
        this.persister.saveEmailOutcome(emailOutcome);
        if(emailOutcome.getId() >= 0) {
            logger.info("The email outcome with ID={} of the email {} has been saved on the DB", emailOutcome.getId(), email);
            JSONObject emailRequest = new JSONObject();
            emailRequest.put("id", emailOutcome.getId());
            emailRequest.put("sender", email.getSender());
            emailRequest.put("receiver", email.getReceiver());
            emailRequest.put("object", email.getObject());
            emailRequest.put("body", email.getBody());
            emailOutcome = this.putEmailInTheQueue(emailRequest, topic);
            this.persister.updateEmailOutcome(emailOutcome);
        }
        else {
            logger.error("The email outcome of the email {} has not been saved on the DB", email);
        }
        return emailOutcome;
    }

    private EmailOutcome putEmailInTheQueue(JSONObject emailRequest, String topic) {
        EmailOutcome emailOutcome = new EmailOutcome();
        emailOutcome.setId(emailRequest.getInt("id"));
        logger.info("Putting in the email queue the email request=[{}]", emailRequest);
        ListenableFuture<SendResult<String, JSONObject>> future = kafkaTemplate.send(topic, emailRequest);
        future.addCallback(new ListenableFutureCallback<SendResult<String, JSONObject>>() {
            @Override
            public void onSuccess(SendResult<String, JSONObject> result) {
                logger.info("The email request=[{}] has been put in the email queue=[{}]" , result.getProducerRecord().value(), result.getProducerRecord().topic());
                emailOutcome.setOutcome(Outcome.ACCEPTED);
                logger.info("The email request with ID={} has been ACCEPTED", emailOutcome.getId());
            }
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to put the email request=[{}] in the email queue because of: {}", emailRequest, ex.getMessage());
                emailOutcome.setOutcome(Outcome.FAILED);
                logger.info("The email request with ID={} FAILED", emailOutcome.getId());
            }
        });
        return emailOutcome;
    }

    @Override
    public List<EmailOutcome> getAllEmailOutcomes() {
        logger.debug("Trying to select all the email outcomes");
        List<EmailOutcome> emailOutcomeList = this.persister.getAllEmailOutcomes();
        if(emailOutcomeList.isEmpty()) {
            logger.error("No one email outcome has been found");
        }
        else {
            logger.info("Selection executed! Has been found {} email outcomes", emailOutcomeList.size());
        }
        return emailOutcomeList;
    }

    @Override
    public EmailOutcome getEmailOutcomeById(int id) {
        logger.debug("Trying to select the email outcome with ID={}", id);
        EmailOutcome emailOutcome = this.persister.getEmailOutcomeById(id);
        if(emailOutcome == null) {
            logger.error("No one email outcome with ID={} has been found", id);
        }
        else {
            logger.info("Selection executed! Email outcome {}", emailOutcome);
        }
        return emailOutcome;
    }
}
