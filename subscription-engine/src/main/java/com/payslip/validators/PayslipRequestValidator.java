/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.validators;

import com.payslip.events.PayslipRequested;

/**
 *
 * @author maliska
 */
public class PayslipRequestValidator implements Validator {

    private PayslipRequested request;

    public PayslipRequestValidator(PayslipRequested request) {
        this.request = request;        
    }
    
    
    @Override
    public void validate() throws Exception {
        
          
    }
    
}
