/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews;

import com.payslip.api.util.RequestParser;
import com.payslip.api.infrastructure.kafka.EventProducer;
import com.payslip.api.infrastructure.ews.exceptions.PayPeriodException;
import com.payslip.api.infrastructure.ews.exceptions.PayPeriodRangeValidator;
import com.payslip.api.infrastructure.ews.validators.PayPeriodValidator;
import com.payslip.api.infrastructure.ews.validators.PayPeriodViewValidator;
import com.payslip.api.infrastructure.ews.validators.ValidatorProcessor;
import com.payslip.lib.common.events.PayslipRequested;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.response.GetItemResponse;
import microsoft.exchange.webservices.data.core.response.ServiceResponseCollection;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.notification.ItemEvent;
import microsoft.exchange.webservices.data.notification.NotificationEvent;
import microsoft.exchange.webservices.data.notification.NotificationEventArgs;
import microsoft.exchange.webservices.data.notification.StreamingSubscriptionConnection;
import microsoft.exchange.webservices.data.notification.SubscriptionErrorEventArgs;
import microsoft.exchange.webservices.data.property.complex.ItemId;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class ExchangeStreamingSubscriber implements StreamingSubscriber {

    private static final Logger logger = Logger.getLogger(ExchangeStreamingSubscriber.class.getName());

    private EventProducer producer;

   private static ExchangeService service;
    public ExchangeStreamingSubscriber(EventProducer producer) {

        this.producer = producer;
    }

    @Override
    public void notificationEventDelegate(Object sender, NotificationEventArgs nev) {
        System.out.println("--- notification event ---");
        logger.log(Level.INFO, "--- Calling ews.getService() ---");
        
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
            // Now retrieve the Subject property of all the new emails in one
            // call to EWS.
            ServiceResponseCollection<GetItemResponse> responses;
            ValidatorProcessor validator = new ValidatorProcessor();
            try {
                responses = service.bindToItems(newMailsIds, new PropertySet(ItemSchema.Subject));

                logger.log(Level.INFO, "count=======" + responses.getCount());

                for (GetItemResponse response : responses) {
                    String subject = response.getItem().getSubject();
                    EmailMessage message = EmailMessage.bind(service, response.getItem().getId());
                    Pattern pattern = Pattern.compile("#PAYSLIP");
                    Matcher matcher = pattern.matcher(response.getItem().getSubject());
                    if (matcher.lookingAt()) {
                        logger.log(Level.INFO, "--- Processing new payslip request: {0} ---", response.getItem().getSubject());
                        PayslipRequested emailRequest = RequestParser.parse(subject, message.getDateTimeSent(), message.getSender().getAddress());
//                         PayslipRequested emailRequest2 = RequestParser.parse2(subject, message.getDateTimeSent(), message.getSender().getAddress());

                        logger.log(Level.INFO, "--- validating request ---");
                        validator.add(new PayPeriodValidator(emailRequest.getPeriodFrom()));
                        validator.add(new PayPeriodValidator(emailRequest.getPeriodTo()));
                        validator.add(new PayPeriodRangeValidator(emailRequest.getPeriodFrom(), emailRequest.getPeriodTo()));
                        validator.add(new PayPeriodViewValidator(emailRequest.getPeriodFrom(), emailRequest.getPeriodTo()));

                        try {
                            validator.process();
                            logger.log(Level.INFO, "--- validation successful ---");
                            logger.log(Level.INFO, "--- Publishing payslip requests to 'payslip.topic'");
                            producer.publish(emailRequest);
                            response.getItem().delete(DeleteMode.MoveToDeletedItems);
                        } catch (PayPeriodException ppe) {
                            //TODO:publish notification to error topic
                            logger.log(Level.WARNING, ppe.getMessage());
                        }
                    } else {
                        logger.log(Level.INFO, "--- Ignoring email: {0} ---", response.getItem().getSubject());
                    }

                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public void subscriptionErrorDelegate(Object sender, SubscriptionErrorEventArgs ser) {
        logger.log(Level.INFO, "--- Subscription error ---" + ser.getException());
        // Cast the sender as a StreamingSubscriptionConnection object.          
        StreamingSubscriptionConnection connection = (StreamingSubscriptionConnection) sender;

        try {
            connection.open();
            logger.log(Level.INFO, "--- Subscription connection opened ---");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    

}
