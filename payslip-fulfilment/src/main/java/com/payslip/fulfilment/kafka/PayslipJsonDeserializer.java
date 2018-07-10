/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment.kafka;

import com.payslip.lib.common.events.PayslipRequested;

/**
 *
 * @author maliska
 */
public class PayslipJsonDeserializer extends JsonDeserializer<PayslipRequested> {

    public PayslipJsonDeserializer() {
        super(PayslipRequested.class);
    }

}
