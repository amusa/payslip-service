package com.payslip.fulfilment;

import com.payslip.common.events.AppEvent;
import com.payslip.fulfilment.kafka.EventConsumer;
import com.payslip.common.events.PayslipRequested;
import com.payslip.fulfilment.kafka.KAFKA;
import com.payslip.fulfilment.kafka.REQUEST;
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

    @Inject
    private FulfilmentService fulfilmentService;

    private EventConsumer eventConsumer;

    int initialDelay = 0;
    int period = 1;

    @REQUEST
    @Inject
    Properties kafkaProperties;

    @Inject
    Event<AppEvent> events;

    public void handle(@Observes PayslipRequested event) {
        logger.log(Level.INFO, "--- Handling event id: {0} ---", event.getId());
        fulfilmentService.when(event);
    }

    @PostConstruct
    public void init() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        logger.log(Level.INFO, "--- ENVIRONMENT VARIABLES: BOOTSTRAP_SERVERS={0} ---", bootstrapServers);

        if (bootstrapServers != null) {
            kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }

        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "payslip-processor");
        kafkaProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        logger.log(Level.INFO, "--- initializing service with properties: {0} ---\n", kafkaProperties);
        String payslips = kafkaProperties.getProperty("payslip.topic");

        eventConsumer = new EventConsumer(kafkaProperties, ev -> {
            logger.log(Level.INFO, "firing = {0}", ev);
            events.fire(ev);
        }, payslips);

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
