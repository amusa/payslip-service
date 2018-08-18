package com.payslip.api.infrastructure.kafka;

import com.payslip.common.events.Notification;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

@ApplicationScoped
public class NotificationEventPublisher extends EventPublisher<Notification> {

    private Producer<String, Notification> producer;

    @NOTICE
    @Inject
    Properties kafkaProperties;

    private static final Logger logger = Logger.getLogger(NotificationEventPublisher.class.getName());

    @PostConstruct
    private void init() {
//        kafkaProperties.put("transactional.id", UUID.randomUUID().toString());
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        logger.log(Level.INFO, "--- ENVIRONMENT VARIABLES: BOOTSTRAP_SERVERS={0} ---", bootstrapServers);

        if (bootstrapServers != null) {
            kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }

        kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        producer = new KafkaProducer<>(kafkaProperties);

        //producer.initTransactions();
    }

    @PreDestroy
    public void close() {
        producer.close();
    }

    @Override
    public String getTopic() {
        String topic = kafkaProperties.getProperty("payslip.topic");
        return topic;
    }

    @Override
    public Producer getProducer() {
        return producer;
    }

}
