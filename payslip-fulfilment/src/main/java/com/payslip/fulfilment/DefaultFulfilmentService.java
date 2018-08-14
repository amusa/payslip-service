/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment;

import com.payslip.common.events.AppEvent;
import com.payslip.fulfilment.infrastructure.jco.PayData;
import com.payslip.common.events.MonthMarker;
import com.payslip.common.events.Notification;
import com.payslip.common.events.Payload;
import com.payslip.common.events.PayslipGenerated;
import com.payslip.common.events.PayslipRequested;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;


/**
 *
 * @author maliska
 */
@ApplicationScoped
public class DefaultFulfilmentService implements FulfilmentService {

     private static final Logger logger = Logger.getLogger(DefaultFulfilmentService.class.getName());
   
    @Inject
    private PayslipService payslipService;

    @Inject
    Event<AppEvent> events;

    @Override
    public void when(PayslipRequested request) {
        logger.log(Level.INFO, "--- Event received for processing ---");
        //TODO:validate request

        try {
            logger.log(Level.INFO, "--- Invoking PayslipService.getPayslipBytes() ---");
            List<PayData> payDataList = payslipService
                    .getPayslipBytes(request.getStaffId(),
                            request.getPeriodFrom().getDate(),
                            request.getPeriodTo().getDate(MonthMarker.END));

            PayslipGenerated event = makePayslipGeneratedEvent(request, payDataList);
            events.fire(event);
           
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, "--- firing notification for error message {0} ---", ex.getMessage());
            
            Notification notice = new Notification(
                    request.getEmailFrom(),
                    request.getDateSent(),                   
                    request.getId(),
                    request.getSubject(),
                    ex.getMessage()
            );
            
            events.fire(notice);
        }

    }

    @PostConstruct
    private void initConsumer() { 
        logger.log(Level.INFO, "--- Default Fulfilment Service Initialized ---");
    }

    private PayslipGenerated makePayslipGeneratedEvent(PayslipRequested request, List<PayData> payDataList) {
        List<Payload> payloads = makePayload(payDataList);
        
        PayslipGenerated event = new PayslipGenerated(
                request.getEmailFrom(),
                request.getDateSent(),
                request.getStaffId(),
                request.getId(),
                request.getSubject(),
                payloads
        );

        logger.log(Level.INFO, "--- PayslipGeneratedEvent created: {0} ---", event);
        
        return event;
    }

    private List<Payload> makePayload(List<PayData> payDataList) {
        List<Payload> payload = new ArrayList<>();

        for (PayData pd : payDataList) {
            String fileName = createFileName(pd);
            Payload pl = new Payload(pd.getPayslipPdf(), fileName);
            payload.add(pl);
        }

        return payload;
    }
    
    private String createFileName(PayData pd) {
        String payType;

        if (pd.getOffCycleReason() == null || "".equals(pd.getOffCycleReason().trim())) {
            payType = "Payslip";
        } else {
            payType = sanitize(pd.getOffCycleReason());
        }

        return String.format("%tb_%tY_%s_%s.pdf",
                pd.getPayDate(),
                pd.getPayDate(),
                payType,
                pd.getStaffId());
    }

    private String sanitize(String text) {
        return text.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
