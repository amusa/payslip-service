/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews.validators;

import com.payslip.api.infrastructure.ews.exceptions.PayPeriodException;
import com.payslip.lib.common.events.PayPeriod;
import java.time.LocalDate;
import java.time.Year;

/**
 *
 * @author maliska
 */
public class PayPeriodViewValidator implements Validator {

    private final PayPeriod fPeriod;
    private final PayPeriod tPeriod;

    public PayPeriodViewValidator(PayPeriod fPeriod, PayPeriod tPeriod) {
        this.fPeriod = fPeriod;
        this.tPeriod = tPeriod;
    }

    @Override
    public void validate() throws PayPeriodException {
        validate(fPeriod);
        validate(tPeriod);       
    }
    
    private void validate(PayPeriod payPeriod) throws PayPeriodException{
        if (payPeriod.getYear() > Year.now().getValue()) {
            throw new PayPeriodException("Cannot view future payslip");
        }

        if (payPeriod.getYear() == Year.now().getValue()) {
            if (payPeriod.getMonth() > LocalDate.now().getMonthValue()) {
                throw new PayPeriodException("Cannot view future payslip");
            } else if (payPeriod.getMonth() == LocalDate.now().getMonthValue()) {
                if (LocalDate.now().getDayOfMonth() < 23) {
                    throw new PayPeriodException("Payslip not due");
                }
            }
        }
    }

}
