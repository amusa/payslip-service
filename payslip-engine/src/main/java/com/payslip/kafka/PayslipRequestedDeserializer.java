package com.payslip.kafka;


import com.payslip.events.PayslipRequested;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class PayslipRequestedDeserializer extends JsonbDeserializer<PayslipRequested> {
  public PayslipRequestedDeserializer() {
    super(PayslipRequested.class);
  }
}
