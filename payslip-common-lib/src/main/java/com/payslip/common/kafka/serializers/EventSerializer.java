package com.payslip.common.kafka.serializers;

import com.payslip.common.events.AppEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventSerializer implements Serializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(EventSerializer.class.getName());

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        // nothing to configure
    }

    @Override
    public byte[] serialize(final String topic, final AppEvent event) {

        logger.log(Level.INFO, "--- serializing event: {0} ---", event);

        try {
            if (event == null) {
                return null;
            }

            final JsonbConfig config = new JsonbConfig()
                    .withAdapters(new UUIDAdapter())
                    .withSerializers(new EventJsonbSerializer());

            final Jsonb jsonb = JsonbBuilder.create(config);
            logger.log(Level.INFO, "--- returning byte: ---");
            return jsonb.toJson(event, AppEvent.class).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.severe("Could not serialize event: " + e.getMessage());
            throw new SerializationException("Could not serialize event", e);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }

}
