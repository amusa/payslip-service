package com.payslip.kafka;

import com.payslip.events.AppEvent;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class AppEventDeserializer extends JsonbDeserializer<AppEvent> {
  public AppEventDeserializer() {
    super(AppEvent.class);
  }
}
