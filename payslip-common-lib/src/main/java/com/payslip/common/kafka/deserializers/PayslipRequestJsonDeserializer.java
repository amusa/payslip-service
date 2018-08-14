/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.kafka.deserializers;

import com.payslip.common.events.PayslipRequested;

/**
 *
 * @author maliska
 */
public class PayslipRequestJsonDeserializer extends GsonDeserializer<PayslipRequested> {

    public PayslipRequestJsonDeserializer() {
        super(PayslipRequested.class);
    }
}
