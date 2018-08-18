package com.payslip.notification.service;

import com.payslip.common.events.AppEvent;
import com.payslip.common.events.Notification;
import com.payslip.common.events.PayslipGenerated;
import com.payslip.notification.infrastructure.kafka.EventConsumer;
import com.payslip.notification.infrastructure.kafka.KAFKA;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import org.apache.kafka.clients.consumer.ConsumerConfig;

@Singleton
@Startup
public class NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());

    @Resource
    ManagedExecutorService mes;

    private EventConsumer eventConsumer;

    int initialDelay = 0;
    int period = 1;

    @KAFKA
    @Inject
    Properties kafkaProperties;

    @Inject
    Event<AppEvent> events;

    @Inject
    Messenger messenger;

    public void handle(@Observes PayslipGenerated event) {
        logger.log(Level.INFO, "--- Handling PayslipGenerated event ---");
        messenger.when(event);
    }

    public void handle(@Observes Notification event) {
        logger.log(Level.INFO, "--- Handling Notification event ---");
        messenger.when(event);
    }

    @PostConstruct
    public void init() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        logger.log(Level.INFO, "--- ENVIRONMENT VARIABLES: BOOTSTRAP_SERVERS={0} ---", bootstrapServers);

        if (bootstrapServers != null) {
            kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }

        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "payslip-notification");
        kafkaProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        logger.log(Level.INFO, "--- initializing service with properties: {0} ---\n", kafkaProperties);
        String payslips = kafkaProperties.getProperty("payslip.payslip.topic");
        String notices = kafkaProperties.getProperty("payslip.response.topic");

        eventConsumer = new EventConsumer(kafkaProperties, ev -> {
            logger.log(Level.INFO, "firing = {0}", ev);
            events.fire(ev);
        }, payslips, notices);

        logger.log(Level.INFO, "--- Submitting eventconsumer task {0} ---", mes.toString());
        mes.submit(eventConsumer);
        logger.log(Level.INFO, "--- Event consumer scheduled with topic {0}", payslips);
    }

    @PreDestroy
    public void close() {
        logger.info("--- PayslipFulfilment destroying ---");
        eventConsumer.stop();
    }
}
