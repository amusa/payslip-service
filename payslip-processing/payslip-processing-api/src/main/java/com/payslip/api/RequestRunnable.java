/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api;

import com.payslip.api.infrastructure.kafka.EventProducer;
import com.payslip.api.service.RequestProcessor;
import com.payslip.api.service.util.PayPeriodException;
import com.payslip.api.service.util.PayPeriodRangeValidator;
import com.payslip.api.service.util.PayPeriodValidator;
import com.payslip.api.service.util.ValidatorProcessor;
import com.payslip.lib.common.events.PayslipRequested;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
public class RequestRunnable implements Runnable {
    
    private EventProducer producer;
    private RequestProcessor processor;
    
    private static final Logger logger = Logger.getLogger(RequestRunnable.class.getName());
    
    public RequestRunnable(EventProducer producer, RequestProcessor processor) {
        this.producer = producer;
        this.processor = processor;
    }
    
    @Override
    public void run() {
        ValidatorProcessor validator = new ValidatorProcessor();
        
        try {
            logger.log(Level.INFO, "--- Pulling email requests from RequestProcessor");
            List<PayslipRequested> requests = processor.pullEmailRequests();
            for (PayslipRequested request : requests) {
                logger.log(Level.INFO, "--- validating request ---");
                validator.add(new PayPeriodValidator(request.getPeriodFrom()));
                validator.add(new PayPeriodValidator(request.getPeriodTo()));
                validator.add(new PayPeriodRangeValidator(request.getPeriodFrom(), request.getPeriodTo()));
                
                try {
                    validator.process();
                    logger.log(Level.INFO, "--- validation successful ---");
                    logger.log(Level.INFO, "--- Publishing payslip requests to 'payslip.topic'");
                    producer.publish(request);
                } catch (PayPeriodException ppe) {
                    //TODO:publish notification to error topic
                    logger.log(Level.WARNING, ppe.getMessage());
                }
                
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
}
