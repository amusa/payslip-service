/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.service.util;

import com.payslip.lib.common.events.PayPeriod;

/**
 *
 * @author maliska
 */
public class PayPeriodValidator implements Validator {

    private final PayPeriod period;

    public PayPeriodValidator(PayPeriod pp) {
        this.period = pp;
    }

    @Override
    public void validate() throws PayPeriodException {
        if (period.getMonth() < 1 || period.getMonth() > 12) {
            throw new PayPeriodException("Pay period out of bound");
        }
    }

}