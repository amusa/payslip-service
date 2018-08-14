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

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class EmailMessenger implements Messenger {

    private ExchangeService service;
    private static final Logger logger = Logger.getLogger(EmailMessenger.class.getName());

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
        try {
            EmailMessage msg = new EmailMessage(service);
            msg.setSubject(String.format("RE:%s", event.getSubject()));
            msg.setBody(MessageBody.getMessageBodyFromText("Please find attached your payslip(s) as requested"));
            EmailAddress fromEmail = new EmailAddress("ayemi.musa@nnpcgroup.com");
            msg.getToRecipients().add(event.getEmailFrom());
            msg.setFrom(fromEmail);

            logger.log(Level.INFO, "--- Attaching payslip ---");

            for (Payload pl : event.getPayloads()) {
                msg.getAttachments().addFileAttachment(pl.getPdfFileName(), pl.getPayslipPdf());
            }

            logger.log(Level.INFO, "--- Sending email ---");
            msg.send();
        } catch (Exception ex1) {
            logger.log(Level.SEVERE, "--- error sending emial ---\n", ex1);
        }

    }

    @Override
    public void when(Notification event) {
        logger.log(Level.INFO, "--- Notification Event received for processing ---");
        try {
            EmailMessage msg = new EmailMessage(service);
            msg.setSubject(String.format("RE:%s", event.getSubject()));
            msg.setBody(MessageBody.getMessageBodyFromText(event.getMessage()));
            EmailAddress fromEmail = new EmailAddress("ayemi.musa@nnpcgroup.com");
            msg.getToRecipients().add(event.getEmailFrom());
            msg.setFrom(fromEmail);
            
            logger.log(Level.INFO, "--- Sending email ---");
            msg.send();
        } catch (Exception ex1) {
            logger.log(Level.SEVERE, "--- error sending emial ---\n", ex1);
        }
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
