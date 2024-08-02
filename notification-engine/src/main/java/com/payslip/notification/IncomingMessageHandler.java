package com.payslip.notification;

import io.quarkus.logging.Log;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.*;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;

import com.payslip.events.AppEvent;
import com.payslip.events.Notification;
import com.payslip.events.PayslipGenerated;
import com.payslip.notification.services.Messenger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class IncomingMessageHandler {

    @Inject
    Messenger messenger;

    @Inject
    Event<AppEvent> events;

    @Acknowledgment(Strategy.PRE_PROCESSING)
    @Incoming("payslip-result")
    public CompletionStage<Void> incomingPayslip(Message<PayslipGenerated> message) {
        Log.infov("--- Payslip notice Event received for processing ---");
        // TODO:validate request
        PayslipGenerated event = message.getPayload();
        messenger.send(event, true);
        //events.fire(event);
        return CompletableFuture.completedFuture(null).thenAccept(i -> {
        });
    }

    @Acknowledgment(Strategy.PRE_PROCESSING)
    @Incoming("payslip-notice")
    public CompletionStage<Void> incomingNotice(Message<Notification> message) {
        Log.infov("--- Notification Event received for processing ---");
        // TODO:validate request
        Notification noticeEvent = message.getPayload();
        messenger.send(noticeEvent, true);

        return CompletableFuture.completedFuture(null).thenAccept(i -> {
        });
    }

}
