/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.notification.infrastructure.ews;

import com.payslip.notification.service.Messenger;
import com.payslip.common.events.Notification;
import com.payslip.common.events.Payload;
import com.payslip.common.events.PayslipGenerated;
import com.payslip.common.events.PayslipResponse;
import com.payslip.notification.infrastructure.mongodb.MongoDbPayslipClient;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class EmailMessenger implements Messenger {

    private ExchangeService service;
    private static final Logger logger = Logger.getLogger(EmailMessenger.class.getName());

    @Inject
    MongoDbPayslipClient dbClient;

    @Inject
    @ConfigProperty(name = "EWS_HOST")
    private String ewsHost;

    @Inject
    @ConfigProperty(name = "EWS_USER")
    private String ewsUser;

    @Inject
    @ConfigProperty(name = "EWS_PASSWORD")
    private String ewsPwd;

    @Inject
    @ConfigProperty(name = "EWS_DOMAIN")
    private String ewsDomain;

    private String ewsUrl;

    @Override
    public void when(PayslipGenerated event) {
        logger.log(Level.INFO, "--- PayslipGenerated Event received for processing ---");

        PayslipResponse payslipResponse = dbClient.getPayslipResponse(event.getReferenceId());

        if (payslipResponse != null) {
            logger.log(Level.INFO, "--- payslip response retried from db ---");

            mailPayslip(payslipResponse, false);
        }

    }

    @Override
    @Retry(maxRetries = 3)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
    public void mailPayslip(PayslipResponse payslip, boolean retry) {
        try {
            EmailMessage msg = new EmailMessage(service);
            msg.setSubject(String.format("RE:%s", payslip.getSubject()));
            msg.setBody(MessageBody.getMessageBodyFromText("Please find attached your payslip(s) as requested"));
            EmailAddress fromEmail = new EmailAddress("ayemi.musa@nnpcgroup.com");
            msg.getToRecipients().add(payslip.getEmailFrom());
            msg.setFrom(fromEmail);

            logger.log(Level.INFO, "--- Attaching payslip ---");

            for (Payload pl : payslip.getPayloads()) {
                msg.getAttachments().addFileAttachment(pl.getPdfFileName(), pl.getPayslipPdf());
            }

            logger.log(Level.INFO, "--- Sending email ---");

            msg.send();
        } catch (Exception ex1) {
            logger.log(Level.SEVERE, "--- error sending email ---\n{0}", ex1);
            if (!retry) {
                markForRetry(payslip.getRequestId());
            }
            return;
        }

        deletePayslip(payslip.getRequestId());
    }

    @Override
    public void when(Notification event) {
        logger.log(Level.INFO, "--- Notification Event received for processing ---");

        mailNotice(event, false);
    }

    @Override
    @Retry(maxRetries = 3)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
    public void mailNotice(Notification notice, boolean retry) {
        try {
            EmailMessage msg = new EmailMessage(service);
            msg.setSubject(String.format("RE:%s", notice.getSubject()));
            msg.setBody(MessageBody.getMessageBodyFromText(notice.getMessage()));
            EmailAddress fromEmail = new EmailAddress("ayemi.musa@nnpcgroup.com");
            msg.getToRecipients().add(notice.getEmailFrom());
            msg.setFrom(fromEmail);

            logger.log(Level.INFO, "--- Sending email ---");
            msg.send();
        } catch (Exception ex1) {
            logger.log(Level.SEVERE, "--- error sending emial ---\n", ex1);
            saveNoticeForRetry(notice);
            return;
        }

        if (retry) {
            deleteNotice(notice.getId());
        }
    }

    private void deletePayslip(String id) {
        dbClient.deletePayslip(id);
    }

    private void deleteNotice(String id) {
        dbClient.deleteNotice(id);
    }

    @Retry(maxRetries = 3)
    public void markForRetry(String id) {
        logger.log(Level.INFO, "--- saving notice to db for deferred retry ---\n");
        dbClient.markAsFailed(id);
        logger.log(Level.INFO, "--- notice saved successfully ---\n");
    }

    @Retry(maxRetries = 3)
    public void saveNoticeForRetry(Notification notice) {
        logger.log(Level.INFO, "--- Marking payload for deferred retry ---\n");
        dbClient.putNoticeForRetry(notice);
        logger.log(Level.INFO, "--- payload marked as failed successfully ---\n");
    }

    @PostConstruct
    private void initConsumer() {
        logger.log(Level.INFO, "--- Initializing Default Fulfilment Service ---");
        ewsUrl = String.format("https://%s/EWS/Exchange.asmx", ewsHost);
        service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

        // Provide Crendentials
        ExchangeCredentials credentials = new WebCredentials(ewsUser,
                ewsPwd, ewsDomain);
        service.setCredentials(credentials);

        try {
            service.setUrl(new URI(ewsUrl));
        } catch (URISyntaxException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "--- Email Messenger Service Initialized ---");
    }

}
