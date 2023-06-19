package com.payslip.payslip;

import io.quarkus.logging.Log;

import java.time.Instant;
import java.util.UUID;

import org.apache.kafka.common.errors.RecordTooLargeException;
import org.eclipse.microprofile.reactive.messaging.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.payslip.events.AppEvent;
import com.payslip.events.Notification;
import com.payslip.events.PayslipGenerated;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class OutgoingMessageHandler {

    @Inject
    @Channel("payslip-result")
    Emitter<PayslipGenerated> payslipEmitter;

    @Inject
    @Channel("payslip-notice")
    Emitter<Notification> noticeEmitter;

    @Inject
    Event<AppEvent> events;

    ObjectMapper mapper;

    {
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    public void handle(@Observes PayslipGenerated payslipEvent) {

        Log.infov("--- PayslipGenerated Event received for processing: {0} ---");

        payslipEmitter.send(payslipEvent)
                .whenComplete((success, failure) -> {
                    if (failure != null) {
                        Log.infov("--- RecordTooLargeException thrown: Preparing to send response ---");
                        processErrorNotification(payslipEvent, failure);                        
                    } else {
                        Log.infov("--- PayslipGenerated Event sent to kafka endpoint successfully ---");
                    }
                });
       
    }

    public void handle(@Observes Notification notificationEvent) {
        try {
            Log.infov("--- Notification Event received for processing: {0} ---",
                    mapper.writeValueAsString(notificationEvent));

        } catch (JsonProcessingException e) {
            Log.errorv("--- Json conversion error occured {0} ---", e.getCause().getMessage());
        }

        noticeEmitter.send(notificationEvent).whenComplete((success, failure) -> {
                    if (failure != null) {
                        Log.infov("--- Error sending message to Kafka: {0} ---", failure.getMessage());                                                
                    } else {
                        Log.infov("--- Notification Event sent to kafka endpoint successfully ---");
                    }
                });       

    }

    private void processErrorNotification(PayslipGenerated payslip, Throwable e) {
        Notification notice = new Notification();
        notice.id = UUID.randomUUID().toString();
        notice.instant = Instant.now();
        notice.emailFrom = payslip.emailFrom;
        notice.dateSent = payslip.dateSent;
        notice.requestId = payslip.id;
        notice.subject = payslip.subject;
        notice.message = "Sorry, the number of payslips requested cannot be transmitted at the moment. Please reduce the period range.\n\n\nDETAILS:\n"
                + e.getMessage();

        try {
            Log.infov("--- Sending RecordTooLargeException exception notification: {0} ---",
                    mapper.writeValueAsString(notice));
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        }

        events.fire(notice);
    }

}
