/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews.validators;

import com.payslip.api.infrastructure.ews.exceptions.PayPeriodException;
import com.payslip.common.events.PayPeriod;
import java.time.LocalDate;
import java.time.Year;

/**
 *
 * @author maliska
 */
public class PayPeriodViewValidator implements Validator {

    private final PayPeriod fPeriod;
    private final PayPeriod tPeriod;
    private final Boolean payDayCheck;

    public PayPeriodViewValidator(PayPeriod fPeriod, PayPeriod tPeriod, Boolean payDayCheck) {
        this.fPeriod = fPeriod;
        this.tPeriod = tPeriod;
        this.payDayCheck = payDayCheck;
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
                if (payDayCheck && LocalDate.now().getDayOfMonth() < 23) {
                    throw new PayPeriodException("Current month payslip will be available as from 23rd day of the month. Please check back.");
                }
            }
        }
    }

}
