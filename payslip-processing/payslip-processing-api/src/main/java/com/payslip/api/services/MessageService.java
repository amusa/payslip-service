/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.services;

import com.payslip.api.infrastructure.kafka.EventPublisher;
import com.payslip.common.events.Notification;
import com.payslip.common.events.PayslipRequested;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class MessageService {
    private static final Logger logger = Logger.getLogger(MessageService.class.getName());

    @Inject
    EventPublisher<PayslipRequested> requestPublisher;

    @Inject
    EventPublisher<Notification> noticePublisher;


    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "--- MessageService initialized ---");
    }

    public void handle(@Observes PayslipRequested event) {
        logger.log(Level.INFO, "--- publishing payslip request to topic '{0}'", requestPublisher.getTopic());
        requestPublisher.publish(event);
        logger.log(Level.INFO, "--- payslip request published successfully ---");
    }

    public void handle(@Observes Notification event) {
        logger.log(Level.INFO, "--- publishing notification to topic '{0}'", noticePublisher.getTopic());
        noticePublisher.publish(event);
        logger.log(Level.INFO, "--- notification published successfully ---");
    }
}
