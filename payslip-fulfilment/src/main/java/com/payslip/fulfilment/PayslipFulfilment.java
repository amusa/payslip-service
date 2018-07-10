package com.payslip.fulfilment;

import com.payslip.fulfilment.kafka.EventConsumer;
import com.payslip.fulfilment.kafka.KAFKA;
import com.payslip.lib.common.events.PayslipRequested;
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
public class PayslipFulfilment {

    private static final Logger logger = Logger.getLogger(PayslipFulfilment.class.getName());

    @Resource
    ManagedExecutorService mes;

    private EventConsumer eventConsumer;

    int initialDelay = 0;
    int period = 1;

    @KAFKA
    @Inject
    Properties kafkaProperties;

    @Inject
    Event<PayslipRequested> events;

    public void handle(@Observes PayslipRequested event) {
        logger.log(Level.INFO, "Handling event {0}", event);
        //broker.when(event);

    }

    @PostConstruct
    public void init() {
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "payslip-processor");
        kafkaProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        logger.log(Level.INFO, "Initializing service with properties: {0}", kafkaProperties);
        String payslips = kafkaProperties.getProperty("payslip.topic");

        eventConsumer = new EventConsumer(kafkaProperties, ev -> {
            logger.log(Level.INFO, "firing = {0}", ev);
            events.fire(ev);
        }, payslips);

//        executor.scheduleAtFixedRate(eventConsumer, initialDelay, period, TimeUnit.MINUTES);
        logger.log(Level.INFO, "--- Submitting eventconsumer task {0}", mes.toString());
        mes.submit(eventConsumer);
        logger.log(Level.INFO, "--- Event consumer scheduled with topic {0}", payslips);
    }

    @PreDestroy
    public void close() {
        logger.info("--- PayslipFulfilment destroying");
        eventConsumer.stop();
    }
}
