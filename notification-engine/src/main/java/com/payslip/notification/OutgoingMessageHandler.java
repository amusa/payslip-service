package com.payslip.notification;

import io.quarkus.logging.Log;
import com.payslip.events.Notification;
import com.payslip.events.PayslipGenerated;
import com.payslip.notification.services.Messenger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class OutgoingMessageHandler {

    @Inject
    Messenger messenger;
    
    public void handle(@Observes PayslipGenerated payslipEvent) {
        Log.infov("--- PayslipGenerated Event received for Email ---");

        messenger.send(payslipEvent, true);

        Log.infov("--- PayslipGenerated Event sent to user successfully ---");
    }

    public void handle(@Observes Notification notificationEvent) {
        Log.infov("--- Notification Event received for Email ---");

        messenger.send(notificationEvent, true);

        Log.infov("--- Notification Event sent to user successfully ---");

    }

}
