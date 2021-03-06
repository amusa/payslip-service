package com.payslip.api.infrastructure.kafka;

import com.payslip.common.events.AppEvent;
import com.payslip.common.events.PayslipRequested;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;

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
public class EventProducer {

    private Producer<String, AppEvent> producer;
    //private String topic;

    @KAFKA
    @Inject
    Properties kafkaProperties;

    private static final Logger logger = Logger.getLogger(EventProducer.class.getName());

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
        //topic = kafkaProperties.getProperty("payslip.request.topic");
        //producer.initTransactions();
    }

    public void publish(AppEvent event, boolean notice) {
        String topic;
        
        if (notice) {
            topic = kafkaProperties.getProperty("payslip.response.topic");
        } else {
            topic = kafkaProperties.getProperty("payslip.request.topic");
        }
        
        final ProducerRecord<String, AppEvent> record = new ProducerRecord<>(topic, event);
        try {
//            producer.beginTransaction();
            logger.info("---publishing = " + record);
            producer.send(record);
//            producer.commitTransaction();
        } catch (ProducerFencedException e) {
            producer.close();
        } catch (KafkaException e) {
//            producer.abortTransaction();
        }
    }

    @PreDestroy
    public void close() {
        producer.close();
    }
}
