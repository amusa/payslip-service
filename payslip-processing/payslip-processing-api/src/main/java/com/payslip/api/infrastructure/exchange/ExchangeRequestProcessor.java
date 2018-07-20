/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.exchange;

import com.payslip.api.RequestParser;
import com.payslip.api.service.RequestProcessor;
import com.payslip.lib.common.events.PayslipRequested;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.notification.GetEventsResults;
import microsoft.exchange.webservices.data.notification.ItemEvent;
import microsoft.exchange.webservices.data.notification.PullSubscription;
import microsoft.exchange.webservices.data.property.complex.FolderId;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class ExchangeRequestProcessor implements RequestProcessor {

    private PullSubscription subscription;
    private ExchangeService service;
    private static final Logger logger = Logger.getLogger(ExchangeRequestProcessor.class.getName());

    @PostConstruct
    private void initConsumer() {
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

        // Subscribe to pull notifications in the Inbox folder, and get notified when a new mail is received, when an item or folder is created, or when an item or folder is deleted.
        List folders = new ArrayList();
        folders.add(new FolderId().getFolderIdFromWellKnownFolderName(WellKnownFolderName.Inbox));

        try {
            logger.log(Level.SEVERE, "Subscribing to PullNotifications:");
            subscription = service.subscribeToPullNotifications(folders, 10 /* timeOut: the subscription will end if the server is not polled within 5 minutes. */,
                    null /* watermark: null to start a new subscription. */, EventType.NewMail);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    public List<PayslipRequested> pullEmailRequests() throws Exception {
        List<PayslipRequested> requests = new ArrayList<>();
        GetEventsResults events = subscription.getEvents();
        logger.log(Level.INFO, "--- events======{0}", events.getItemEvents());

        for (ItemEvent itemEvent : events.getItemEvents()) {
            if (itemEvent.getEventType() == EventType.NewMail) {
                EmailMessage message = EmailMessage.bind(service, itemEvent.getItemId());
                if (message.getSubject().startsWith("#PAYSLIP")) {
                    logger.log(Level.INFO, "--- Processing new payslip request: {0} ---", message.getSubject());
                    PayslipRequested emailRequest = RequestParser.parse(message.getSubject(), message.getDateTimeSent(), message.getSender().getAddress());
                    requests.add(emailRequest);
                } else {
                    logger.log(Level.INFO, "--- Ignoring email: {0} ---", message.getSubject());
                }

            }
        }

        return requests;
    }
}
