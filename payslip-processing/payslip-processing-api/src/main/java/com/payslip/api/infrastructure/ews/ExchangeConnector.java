/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews;

import com.payslip.api.infrastructure.kafka.EventProducer;
import java.net.URI;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.notification.StreamingSubscription;
import microsoft.exchange.webservices.data.notification.StreamingSubscriptionConnection;
import microsoft.exchange.webservices.data.property.complex.FolderId;

/**
 *
 * @author maliska
 */
@Singleton
@Startup
public class ExchangeConnector {

    private static final Logger logger = Logger.getLogger(ExchangeConnector.class.getName());
    private ExchangeService service;
    private static StreamingSubscriptionConnection conn;
    public static StreamingSubscription subscription;
    private final String URL = "https://mail.nnpcgroup.com/EWS/Exchange.asmx";

    @Inject
    EventProducer producer;
    
    @PostConstruct
    private void init() {
        service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials("18359", "M@dan1sc0", "chq");
        service.setCredentials(credentials);
        
        try {
            service.setUrl(new URI(URL));
            WellKnownFolderName sd = WellKnownFolderName.Inbox;
            FolderId folderId = new FolderId(sd);

            ArrayList<FolderId> folder = new ArrayList<FolderId>();
            folder.add(folderId);

            subscription = service.subscribeToStreamingNotifications(folder, EventType.NewMail);

            conn = new StreamingSubscriptionConnection(service, 30);
            conn.addSubscription(subscription);
            StreamingSubscriptionListener streamListener = new StreamingSubscriptionListener(service, producer);
            conn.addOnNotificationEvent(streamListener);
            conn.addOnDisconnect(streamListener);
            conn.open();

            logger.log(Level.INFO, "--- Payslip request streamingsubscribtion subscribed ---");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
