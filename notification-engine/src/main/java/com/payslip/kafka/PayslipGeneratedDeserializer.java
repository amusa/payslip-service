package com.payslip.kafka;

import com.payslip.events.PayslipGenerated;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class PayslipGeneratedDeserializer extends JsonbDeserializer<PayslipGenerated> {
  public PayslipGeneratedDeserializer() {
    super(PayslipGenerated.class);
  }
}
