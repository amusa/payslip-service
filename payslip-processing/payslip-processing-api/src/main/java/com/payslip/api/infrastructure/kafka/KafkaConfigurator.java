package com.payslip.api.infrastructure.kafka;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class KafkaConfigurator {

    private Properties kafkaProperties;
    private Properties kafkaRequestProperties;
    private Properties kafkaResponseProperties;

    @PostConstruct
    private void initProperties() {
        try {
            kafkaProperties = new Properties();
            kafkaRequestProperties = new Properties();
            kafkaResponseProperties = new Properties();

            kafkaProperties.load(KafkaConfigurator.class.getResourceAsStream("/kafka.properties"));
            kafkaRequestProperties.load(KafkaConfigurator.class.getResourceAsStream("/kafka_request.properties"));
            kafkaResponseProperties.load(KafkaConfigurator.class.getResourceAsStream("/kafka_notice.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @KAFKA
    @Produces
    //@RequestScoped
    public Properties exposeKafkaProperties() throws IOException {
        final Properties properties = new Properties();
        properties.putAll(kafkaProperties);
        return properties;
    }

    @REQUEST
    @Produces
    //@RequestScoped
    public Properties exposeKafkaRequestProperties() throws IOException {
        final Properties properties = new Properties();
        properties.putAll(kafkaRequestProperties);
        return properties;
    }

    @NOTICE
    @Produces
    //@RequestScoped
    public Properties exposeKafkaErrorProperties() throws IOException {
        final Properties properties = new Properties();
        properties.putAll(kafkaResponseProperties);
        return properties;
    }
}
