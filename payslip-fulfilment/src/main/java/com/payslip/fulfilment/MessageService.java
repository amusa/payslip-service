/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment;

import com.payslip.common.events.AppEvent;
import com.payslip.common.events.Notification;
import com.payslip.fulfilment.kafka.EventPublisher;
import com.payslip.common.events.PayslipGenerated;
import com.payslip.fulfilment.kafka.EventProducer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author maliska
 */
@Singleton
@Startup
public class MessageService {

    private static final Logger logger = Logger.getLogger(MessageService.class.getName());

//    @Inject
//    EventPublisher<PayslipGenerated> payslipPublisher;
//    
//    @Inject
//    EventPublisher<Notification> noticePublisher;
    @Inject
    EventProducer producer;

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "--- MessageService initialized ---");
    }

    public void handle(@Observes PayslipGenerated event) {
        logger.log(Level.INFO, "--- publishing payslip response to topic '{0}'");
        producer.publish(event);
        logger.log(Level.INFO, "--- payslip response published successfully ---");
    }

    public void handle(@Observes Notification event) {
        logger.log(Level.INFO, "--- publishing notification to topic '{0}'");
        producer.publish(event);
        logger.log(Level.INFO, "--- notification published successfully ---");
    }
}
