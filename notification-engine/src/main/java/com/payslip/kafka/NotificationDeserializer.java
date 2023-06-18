package com.payslip.kafka;

import com.payslip.events.Notification;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class NotificationDeserializer extends JsonbDeserializer<Notification> {
  public NotificationDeserializer() {
    super(Notification.class);
  }
}
