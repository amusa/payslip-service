/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews.exceptions;

import com.payslip.api.infrastructure.ews.validators.Validator;
import com.payslip.common.events.PayPeriod;

/**
 *
 * @author maliska
 */
public class PayPeriodRangeValidator implements Validator {

    private final PayPeriod fPeriod;
    private final PayPeriod tPeriod;

    public PayPeriodRangeValidator(PayPeriod fPeriod, PayPeriod tPeriod) {
        this.fPeriod = fPeriod;
        this.tPeriod = tPeriod;
    }

    @Override
    public void validate() throws PayPeriodException {
        if (fPeriod.getYear() > tPeriod.getYear()) {
            throw new PayPeriodException("Pay period range out of order");
        }

        if (fPeriod.getYear() == tPeriod.getYear()) {
            if (fPeriod.getMonth() > tPeriod.getMonth()) {
                throw new PayPeriodException("Pay period range out of order");
            }
        }
    }

}
