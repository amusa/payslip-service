/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.kafka;

import java.util.logging.Logger;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;

/**
 *
 * @author maliska
 * @param <E>
 */
public abstract class EventPublisher<E> {

    private static final Logger logger = Logger.getLogger(EventPublisher.class.getName());

    
    public void publish(E event) {
        String topic = getTopic();
        final ProducerRecord<String, E> record = new ProducerRecord<>(topic, event);
        try {
//            producer.beginTransaction();
            logger.info("---publishing = " + record);
            getProducer().send(record);
//            producer.commitTransaction();
        } catch (ProducerFencedException e) {
            getProducer().close();
        } catch (KafkaException e) {
//            producer.abortTransaction();
        }
    }

    public abstract String getTopic();
    public abstract Producer getProducer();

}
