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
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
@RestController
public class EmailDequeuerImpl implements EmailDequeuer {

    private static final Logger logger = LoggerFactory.getLogger(EmailDequeuerImpl.class);
    private JavaMailSender emailSender;
    private EmailOutcomePersister persister;
    private KafkaTemplate kafkaTemplate;

    private static final String RESPONSE_STRING_FORMAT = "%s %s greeter => '%s' : %d\n";

    private final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    private static final String HOSTNAME = parseContainerIdFromHostname(System.getenv().getOrDefault("HOSTNAME", "unknown"));

    static String parseContainerIdFromHostname(String hostname) {
        return hostname.replaceAll("magnews-dequeuermail-service-", "").trim();
    }

    @Autowired
    public EmailDequeuerImpl(JavaMailSender emailSender, EmailOutcomePersister persister, KafkaTemplate kafkaTemplate) {
        this.persister = persister;
        this.emailSender = emailSender;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(value="/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String consumeCloudEvent(@RequestHeader Map<String, Object> headers, @RequestBody String body) {
        logger.debug("Received event headers: {}", headers);
        logger.debug("Received event body: {}", body);
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(headers.get("content-type").toString())) {
            logger.info("Consumed event -> Data: {}", body);
        }
        else {
            logger.error("Invalid event format received");
        }
        JSONObject response = new JSONObject(body).put("host", HOSTNAME).put("time",SDF.format(new Date()));
        return response.toString();
    }

    @Override
    @KafkaListener(topics = "magnews-topic", groupId = "magnews-dequeuermail-consumer-group-id")
    public Email readEmailRequestFromEmailQueue(String stringEmailRequest) {
        logger.debug("Trying to read the email request {} on the email queue", stringEmailRequest);
        logger.info("Consumed event -> Data: {}", stringEmailRequest);
        Email email = new Email();
        EmailOutcome emailOutcome = new EmailOutcome();
        JSONObject emailRequest = new JSONObject(stringEmailRequest);
        emailOutcome.setId(emailRequest.getInt("id"));
        email.setSender(emailRequest.getString("sender"));
        email.setReceiver(emailRequest.getString("receiver"));
        email.setObject(emailRequest.getString("object"));
        email.setBody(emailRequest.getString("body"));
        logger.info("The email request {} has been read from the queue", emailRequest);
        try {
            logger.debug("Trying to send email {}", email);
            this.sendEmail(email);
            logger.info("Email {} sent with SUCCESS!", email);
            emailOutcome.setOutcome(Outcome.SUCCESS);
            logger.debug("The email outcome {} of the email {} will be updated into DB", emailOutcome, email);
            this.persister.updateEmailOutcome(emailOutcome);
            logger.info("The email outcome {} of the email {} has been updated into DB", emailOutcome, email);
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("The sent of the email {} FAILED because of the following exception: {}", email, e);
            emailOutcome.setOutcome(Outcome.FAILED);
            logger.debug("The email outcome {} of the email {} will be updated into DB", emailOutcome, email);
            this.persister.updateEmailOutcome(emailOutcome);
            logger.info("The email outcome {} of the email {} has been updated into DB", emailOutcome, email);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("The sent of the email {} FAILED because of the following exception: {}", email, e);
            emailOutcome.setOutcome(Outcome.FAILED);
            logger.debug("The email outcome {} of the email {} will be updated into DB", emailOutcome, email);
            this.persister.updateEmailOutcome(emailOutcome);
            logger.info("The email outcome {} of the email {} has been updated into DB", emailOutcome, email);
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

    @GetMapping("/healthz")
    public String health() {
        return "OK";
    }
}
