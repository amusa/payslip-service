package com.payslip.common.kafka.deserializers;

import com.payslip.common.events.AppEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventDeserializer implements Deserializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(EventDeserializer.class.getName());

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        // nothing to configure
    }

    @Override
    public AppEvent deserialize(final String topic, final byte[] data) {
        logger.log(Level.INFO, "--- deserializing data: {0} ---", data);
        
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            logger.log(Level.INFO, "--- creating jsonObject ---");
            final JsonObject jsonObject = Json.createReader(input).readObject();
            logger.log(Level.INFO, "--- jsonObject created: {0} ---", jsonObject);
            final Class<? extends AppEvent> eventClass = (Class<? extends AppEvent>) Class.forName(jsonObject.getString("class"));
            AppEvent event = eventClass.getConstructor(JsonObject.class).newInstance(jsonObject.getJsonObject("data"));
            logger.log(Level.INFO, "--- returning event: {0} ---", event);
            return event;
        } catch (Exception e) {
            logger.severe("Could not deserialize event: " + e.getMessage());
            throw new SerializationException("Could not deserialize event", e);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }

}
