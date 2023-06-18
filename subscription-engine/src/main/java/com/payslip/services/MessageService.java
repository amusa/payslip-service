/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.payslip.events.Notification;
import com.payslip.events.PayslipRequested;
import com.payslip.exceptions.PayPeriodRangeValidator;
import com.payslip.subscription.NewMessageNotification;
import com.payslip.util.RequestParser;
import com.payslip.validators.PayPeriodValidator;
import com.payslip.validators.PayPeriodViewValidator;
import com.payslip.validators.ValidatorProcessor;

import io.quarkus.logging.Log;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class MessageService {

    @ConfigProperty(name = "app.payDayCheck")
    private Boolean payDayCheck;

    @Inject
    @Channel("payslip-request")
    Emitter<PayslipRequested> emitter;

    @Inject
    @Channel("payslip-notice")
    Emitter<Notification> noticeEmitter;

    ObjectMapper mapper;

    {
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @PostConstruct
    public void init() {
        Log.info("--- MessageService initialized ---");
    }

    public void handle(@Observes NewMessageNotification event) {
        try {
            Log.infov("--- New message event received ---", mapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            Log.infov("--- Error converting to Json NewMessageNotification ---");
            e.printStackTrace();
        }

        ValidatorProcessor validator = new ValidatorProcessor();
        try {

            Pattern pattern = Pattern.compile("#PAYSLIP");
            Matcher matcher = pattern.matcher(event.subject.toUpperCase());

            if (matcher.lookingAt()) {
                Log.infov("--- Processing new payslip request: {0} ---",
                        event.subject);
                PayslipRequested emailRequest;

                try {
                    emailRequest = RequestParser.parse(event.subject, event.sentDateTime,
                            event.sender);

                    Log.info("--- validating request ---");
                    validator.add(new PayPeriodValidator(emailRequest.periodFrom));
                    validator.add(new PayPeriodValidator(emailRequest.periodTo));
                    validator.add(
                            new PayPeriodRangeValidator(emailRequest.periodFrom, emailRequest.periodTo));
                    validator.add(new PayPeriodViewValidator(emailRequest.periodFrom,
                            emailRequest.periodTo, payDayCheck));
                    Log.infov("--- pay period view check set to {0} ---", payDayCheck);

                    validator.process();
                    Log.infov("--- validation successful ---");
                    Log.infov("--- Publishing payslip requests to topic {0}",
                            mapper.writeValueAsString(emailRequest));

                    emitter.send(emailRequest);

                    Log.infov("--- payslip request published successfully ---");
                    // TODO:delete email or mark as read
                    // response.getItem().delete(DeleteMode.MoveToDeletedItems);
                    // Log.infov( "--- mail deleted successfully ---");
                } catch (Exception ex) {
                    Log.warnv("---Error Parsing request or converting to json string", ex.getMessage());
                    publishError(event.subject, event.sentDateTime, event.sender,
                            UUID.randomUUID().toString(), ex.getMessage());
                }
            } else {
                Log.infov("--- Ignoring email: {0} ---", event.subject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    private void publishError(String subject, LocalDateTime dateSent, String senderEmail, String requestId,
            String msg) {
        Notification errorOccurred = new Notification();
        errorOccurred.emailFrom = senderEmail;
        errorOccurred.dateSent = dateSent;
        errorOccurred.requestId = requestId;
        errorOccurred.subject = subject;
        errorOccurred.message = msg;
        Log.infov("--- Publishing error notification to '{0}' topic", errorOccurred);
        noticeEmitter.send(errorOccurred);

    }

}
