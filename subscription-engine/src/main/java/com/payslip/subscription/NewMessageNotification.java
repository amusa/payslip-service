package com.payslip.subscription;

import java.time.LocalDateTime;

public class NewMessageNotification {
    public final String subject;

    public final String id;

    public final String sender;

    public final LocalDateTime sentDateTime;

    public NewMessageNotification(String newId, String newSubject, String newSender, LocalDateTime newSentDateTime) {
        subject = newSubject;
        id = newId;
        sender = newSender;
        sentDateTime = newSentDateTime;
    }
}
