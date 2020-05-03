package com.example.magnewsdequeuermail.services.impl;

import com.example.magnewsdequeuermail.model.Email;
import com.example.magnewsdequeuermail.model.EmailOutcome;
import com.example.magnewsdequeuermail.model.Outcome;
import com.example.magnewsdequeuermail.persistence.EmailOutcomePersister;
import com.example.magnewsdequeuermail.services.EmailDequeuer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.util.Map;

@Service
@RestController
public class EmailDequeuerImpl implements EmailDequeuer {

    private static final Logger logger = LoggerFactory.getLogger(EmailDequeuerImpl.class);
    private JavaMailSender emailSender;
    private EmailOutcomePersister persister;
    private KafkaTemplate kafkaTemplate;

    @Autowired
    public EmailDequeuerImpl(JavaMailSender emailSender, EmailOutcomePersister persister, KafkaTemplate kafkaTemplate) {
        this.persister = persister;
        this.emailSender = emailSender;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(value="/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> consumeCloudEvent(@RequestHeader Map<String, Object> headers, @RequestBody String body) {
        logger.info("Received event headers: {}", headers);
        logger.info("Received event body: {}", body);
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(headers.get("content-type").toString())) {
            logger.info("Consumed event -> Data=[{}]", body);
            return ResponseEntity.ok().build();
        }
        else {
            logger.error("Invalid event format received");
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @KafkaListener(topics = "magnews-topic", groupId = "magnews-dequeuermail-consumer-group-id")
    public Email readEmailRequestFromEmailQueue(JSONObject emailRequest) {
        logger.info("Trying to read a request on the email queue");
        logger.info("Consumed event -> Data=[{}]", emailRequest.toString());
        Email email = new Email();
        EmailOutcome emailOutcome = new EmailOutcome();
        int emailOutcomeId = emailRequest.getInt("id");
        email.setSender(emailRequest.getString("sender"));
        email.setReceiver(emailRequest.getString("receiver"));
        email.setObject(emailRequest.getString("object"));
        email.setBody(emailRequest.getString("body"));
        emailOutcome.setId(emailOutcomeId);
        logger.info("Email request with ID={} for the email {} has been read from the queue", emailOutcomeId, email);
        try {
            this.sendEmail(email);
            emailOutcome.setOutcome(Outcome.SUCCESS);
            this.persister.updateEmailOutcome(emailOutcome);
            logger.info("Email {} sent successfully!", email);
        } catch (MessagingException e) {
            e.printStackTrace();
            emailOutcome.setOutcome(Outcome.FAILED);
            this.persister.updateEmailOutcome(emailOutcome);
            logger.error("Email {} was NOT sent successfully because of the following exception: {}", email, e);
        } catch (Exception e) {
            e.printStackTrace();
            emailOutcome.setOutcome(Outcome.FAILED);
            this.persister.updateEmailOutcome(emailOutcome);
            logger.error("Email {} was NOT sent successfully because of the following exception: {}", email, e);
        }
        return email;
    }

    @Override
    public EmailOutcome sendEmail(Email email) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email.getReceiver());
        message.setSubject(email.getObject());
        message.setText(email.getBody());

        emailSender.send(message);

        return null;
    }
}
