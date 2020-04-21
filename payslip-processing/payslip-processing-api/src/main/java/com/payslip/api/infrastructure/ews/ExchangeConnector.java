/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews;

import com.payslip.api.infrastructure.kafka.EventPublisher;
import com.payslip.api.infrastructure.ews.exceptions.PayPeriodException;
import com.payslip.api.infrastructure.ews.exceptions.PayPeriodRangeValidator;
import com.payslip.api.infrastructure.ews.validators.PayPeriodValidator;
import com.payslip.api.infrastructure.ews.validators.PayPeriodViewValidator;
import com.payslip.api.infrastructure.ews.validators.ValidatorProcessor;
import com.payslip.api.infrastructure.kafka.EventProducer;
import com.payslip.api.util.RequestParser;
import com.payslip.common.events.AppEvent;
import com.payslip.common.events.Notification;
import com.payslip.common.events.PayslipRequested;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.response.GetItemResponse;
import microsoft.exchange.webservices.data.core.response.ServiceResponseCollection;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.notification.ItemEvent;
import microsoft.exchange.webservices.data.notification.NotificationEvent;
import microsoft.exchange.webservices.data.notification.NotificationEventArgs;
import microsoft.exchange.webservices.data.notification.StreamingSubscription;
import microsoft.exchange.webservices.data.notification.StreamingSubscriptionConnection;
import microsoft.exchange.webservices.data.notification.SubscriptionErrorEventArgs;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 *
 * @author maliska
 */
@Singleton
@Startup
public class ExchangeConnector implements StreamingSubscriber {

    private static final Logger logger = Logger.getLogger(ExchangeConnector.class.getName());
    private static ExchangeService service;
    private static StreamingSubscriptionConnection conn;
    private static StreamingSubscription subscription;

    @Inject
    @ConfigProperty(name = "EWS_USER")
    private String ewsUser;

    @Inject
    @ConfigProperty(name = "EWS_PASSWORD")
    private String ewsPwd;

    @Inject
    @ConfigProperty(name = "EWS_DOMAIN")
    private String ewsDomain;

    @Inject
    @ConfigProperty(name = "EWS_HOST")
    private String ewsHost;
    
    @Inject
    @ConfigProperty(name = "PAY_DAY_CHECK")
    private Boolean payDayCheck;

    private String ewsUrl;

    @Inject
    Event<AppEvent> events;

    @PostConstruct
    private void init() {
        try {
            connect();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void notificationEventDelegate(Object sender, NotificationEventArgs nev) {
        logger.log(Level.INFO, "--- payslip request event triggered ---");

        // First retrieve the IDs of all the new emails
        List<ItemId> newMailsIds = new ArrayList<ItemId>();

        Iterator<NotificationEvent> it = nev.getEvents().iterator();
        while (it.hasNext()) {
            ItemEvent itemEvent = (ItemEvent) it.next();
            if (itemEvent != null) {
                newMailsIds.add(itemEvent.getItemId());
            }
        }

        if (newMailsIds.size() > 0) {
            ServiceResponseCollection<GetItemResponse> responses;
            ValidatorProcessor validator = new ValidatorProcessor();
            try {
                responses = service.bindToItems(newMailsIds, new PropertySet(ItemSchema.Subject));

                logger.log(Level.INFO, "=== count: {0} ===", responses.getCount());

                for (GetItemResponse response : responses) {
                    String subject = response.getItem().getSubject();
                    EmailMessage message = EmailMessage.bind(service, response.getItem().getId());
                    Pattern pattern = Pattern.compile("#PAYSLIP");
                    Matcher matcher = pattern.matcher(response.getItem().getSubject().toUpperCase());

                    if (matcher.lookingAt()) {
                        logger.log(Level.INFO, "--- Processing new payslip request: {0} ---", response.getItem().getSubject());
                        PayslipRequested emailRequest;

                        try {
                            emailRequest = RequestParser.parse(subject, message.getDateTimeSent(), message.getSender().getAddress());

                            logger.log(Level.INFO, "--- validating request ---");
                            validator.add(new PayPeriodValidator(emailRequest.getPeriodFrom()));
                            validator.add(new PayPeriodValidator(emailRequest.getPeriodTo()));
                            validator.add(new PayPeriodRangeValidator(emailRequest.getPeriodFrom(), emailRequest.getPeriodTo()));
                            validator.add(new PayPeriodViewValidator(emailRequest.getPeriodFrom(), emailRequest.getPeriodTo(), payDayCheck));
                            logger.log(Level.INFO, "--- pay period view check set to {0} ---", payDayCheck);

                            validator.process();
                            logger.log(Level.INFO, "--- validation successful ---");
                            logger.log(Level.INFO, "--- Publishing payslip requests to topic '{0}'");

                            fireEvent(emailRequest);

                            logger.log(Level.INFO, "--- request published successfully ---");
                            response.getItem().delete(DeleteMode.MoveToDeletedItems);
                            logger.log(Level.INFO, "--- mail deleted successfully ---");
                        } catch (Exception ex) {
                            logger.log(Level.WARNING, ex.getMessage());
                            publishError(subject, message.getDateTimeSent(), message.getSender().getAddress(), UUID.randomUUID().toString(), ex.getMessage());
                        }
                    } else {
                        logger.log(Level.INFO, "--- Ignoring email: {0} ---", response.getItem().getSubject());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void publishError(String subject, Date dateSent, String senderEmail, String requestId, String msg) {
        Notification errorOccurred = new Notification(
                senderEmail,
                dateSent,
                requestId,
                subject,
                msg
        );
        logger.log(Level.INFO, "--- Publishing error notification to '{0}' topic");
        //producer.publish(errorOccurred, true);
        fireEvent(errorOccurred);
    }

    @Override
    public void subscriptionErrorDelegate(Object sender, SubscriptionErrorEventArgs ser) {
        logger.log(Level.INFO, "--- Subscription error ---\n{0}", ser.getException());
        // Cast the sender as a StreamingSubscriptionConnection object.          
        StreamingSubscriptionConnection connection = (StreamingSubscriptionConnection) sender;
        try {
            reconnect(connection);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "--- Error: connection failure: {0} ---", ex.getMessage());
            System.exit(1);
        }

    }

    @Override
    @Retry(maxRetries = 5, maxDuration = 100000, retryOn = {RuntimeException.class, EJBException.class, Exception.class})
    public void reconnect(StreamingSubscriptionConnection connection) throws Exception {
        //try {
        logger.log(Level.INFO, "--- Reconnecting ---");
        connection.open();
        logger.log(Level.INFO, "--- Subscription connection opened ---");
        //} catch (Exception ex) {
        //    logger.log(Level.SEVERE, "--- Error: connection failure: {0} ---", ex.getMessage());
        //    throw new RuntimeException(ex);
        //}
    }

    private void fireEvent(AppEvent event) {
        events.fire(event);
    }

    @Override
    @Retry(maxRetries = 5, maxDuration = 1000000, retryOn = {RuntimeException.class, EJBException.class, Exception.class})
    public void connect() throws Exception {
        logger.log(Level.INFO, "--- initializing Exchange Connector ---");

        ewsUrl = String.format("https://%s/EWS/Exchange.asmx", ewsHost);

        service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(ewsUser, ewsPwd, ewsDomain);
        service.setCredentials(credentials);
        service.setUrl(new URI(ewsUrl));

        WellKnownFolderName sd = WellKnownFolderName.Inbox;
        FolderId folderId = new FolderId(sd);

        ArrayList<FolderId> folder = new ArrayList<FolderId>();
        folder.add(folderId);

        logger.log(Level.INFO, "--- Creating subscribtion ---");
        subscription = service.subscribeToStreamingNotifications(folder, EventType.NewMail);
        logger.log(Level.INFO, "--- Creating streaming subscription connection ---");
        conn = new StreamingSubscriptionConnection(service, 30);
        logger.log(Level.INFO, "--- Subscribing to new mail events ---");
        conn.addSubscription(subscription);

        conn.addOnNotificationEvent(this);
        conn.addOnSubscriptionError(this);
        conn.addOnDisconnect(this);

        logger.log(Level.INFO, "--- Opening connection ---");
        conn.open();
        logger.log(Level.INFO, "--- Connection opened. ---");

        logger.log(Level.INFO, "--- Payslip request streamingsubscribtion subscribed ---");

    }

}
