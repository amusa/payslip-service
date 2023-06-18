package com.payslip.payslip;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.reactive.messaging.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.payslip.events.Notification;
import com.payslip.events.PayslipGenerated;

import jakarta.enterprise.context.ApplicationScoped;
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

    ObjectMapper mapper;

    {
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    public void handle(@Observes PayslipGenerated payslipEvent) {

        Log.infov("--- PayslipGenerated Event received for processing: {0} ---");

        payslipEmitter.send(payslipEvent);

        Log.infov("--- PayslipGenerated Event sent to kafka endpoint successfully ---");
    }

    public void handle(@Observes Notification notificationEvent) {
        try {
            Log.infov("--- Notification Event received for processing: {0} ---",
                    mapper.writeValueAsString(notificationEvent));
        } catch (JsonProcessingException e) {
            Log.errorv("--- Json conversion error occured {0} ---", e.getCause().getMessage());
        }

        noticeEmitter.send(notificationEvent);

        Log.infov("--- Notification Event sent to kafka endpoint successfully ---");

    }

}
