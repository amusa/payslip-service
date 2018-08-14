package com.payslip.fulfilment.kafka;

import com.payslip.common.events.AppEvent;
import com.payslip.common.events.PayslipRequested;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventConsumer implements Runnable {

    private static final Logger logger = Logger.getLogger(EventConsumer.class.getName());

    private final KafkaConsumer<String, AppEvent> consumer;
    private final Consumer<AppEvent> eventConsumer;
    private final AtomicBoolean closed = new AtomicBoolean();

    public EventConsumer(Properties kafkaProperties, Consumer<AppEvent> eventConsumer, String... topics) {
        this.eventConsumer = eventConsumer;
        consumer = new KafkaConsumer<>(kafkaProperties);
        consumer.subscribe(asList(topics));
    }

    @Override
    public void run() {
        try {
            while (!closed.get()) {
                consume();
                logger.log(Level.INFO, "--- Consume blocking ends:");
            }
        } catch (WakeupException e) {
            // will wakeup for closing
            logger.log(Level.INFO, "--- WakeupExcepion: {0}", e);
        } catch (Exception ex) {
            logger.log(Level.INFO, "--- Catch-all exception: {0}", ex);
        } finally {
            logger.log(Level.INFO, "--- Closing consumer closed.get()={0}", closed.get());
            consumer.close();
        }
    }

    private void consume() {
        logger.info("--- Listening for events");
        ConsumerRecords<String, AppEvent> records = consumer.poll(Long.MAX_VALUE);
        logger.log(Level.INFO, "--- Events found: {0}", records.count());

        for (ConsumerRecord<String, AppEvent> record : records) {
            logger.log(Level.INFO, "--- Processing events: {0}", record.value());
            eventConsumer.accept(record.value());
        }

        consumer.commitSync();
    }

    public void stop() {
        logger.info("--- Stopping EventConsumer");
        closed.set(true);
        consumer.wakeup();
    }

}
