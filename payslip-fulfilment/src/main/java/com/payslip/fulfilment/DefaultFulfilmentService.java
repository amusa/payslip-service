/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment;

import com.payslip.fulfilment.infrastructure.jco.PayData;
import com.payslip.lib.common.events.PayslipRequested;
import com.sap.conn.jco.JCoException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class DefaultFulfilmentService implements FulfilmentService {

    private ExchangeService service;
    private static final Logger logger = Logger.getLogger(DefaultFulfilmentService.class.getName());

    @Inject
    private PayslipService payslipService;

    @Override
    public void when(PayslipRequested request) {
        logger.log(Level.INFO, "--- Event received for processing ---");
        try {
            logger.log(Level.INFO, "--- Invoking PayslipService.getPayslipBytes() ---");
            List<PayData> payDataList = payslipService.getPayslipBytes(request.getStaffId(), request.getPeriodFrom().getDate(), request.getPeriodTo().getDate());

            sendEmail(request, payDataList);
        } catch (JCoException ex) {
            Logger.getLogger(DefaultFulfilmentService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DefaultFulfilmentService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    private void initConsumer() {
        logger.log(Level.INFO, "--- Initializing Default Fulfilment Service ---");
        String url = "https://mail.nnpcgroup.com/EWS/Exchange.asmx";

        service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

        // Provide Crendentials
        ExchangeCredentials credentials = new WebCredentials("18359",
                "M@dan1sc0", "chq");
        service.setCredentials(credentials);

        try {
            service.setUrl(new URI(url));
        } catch (URISyntaxException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "--- Default Fulfilment Service Initialized ---");
    }

    private void sendEmail(PayslipRequested request, List<PayData> payDataList) {

        try {
            EmailMessage msg = new EmailMessage(service);
            msg.setSubject("Payslip Fulfilment");
            msg.setBody(MessageBody.getMessageBodyFromText("Here comes your payslip as requested"));
            EmailAddress fromEmail = new EmailAddress("ayemi.musa@nnpcgroup.com");
            msg.getToRecipients().add(request.getEmailFrom());
            msg.setFrom(fromEmail);

            logger.log(Level.INFO, "--- Attaching payslip ---");

            for (PayData pd : payDataList) {
                String fileName = createFileName(pd);
                msg.getAttachments().addFileAttachment(fileName, pd.getPayslipPdf());
            }
            logger.log(Level.INFO, "--- Sending email ---");
            msg.send();
        } catch (Exception ex1) {
            logger.log(Level.SEVERE, null, ex1);
        }

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
