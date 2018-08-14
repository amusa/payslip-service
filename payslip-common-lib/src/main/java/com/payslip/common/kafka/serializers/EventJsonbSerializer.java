package com.payslip.common.kafka.serializers;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import com.payslip.common.events.AppEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventJsonbSerializer implements JsonbSerializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(EventJsonbSerializer.class.getName());
        
    @Override
    public void serialize(final AppEvent event, final JsonGenerator generator, final SerializationContext ctx) {
        logger.log(Level.INFO, "--- serializing event: {0} ---", event);
        generator.write("class", event.getClass().getCanonicalName());
        generator.writeStartObject("data");
        ctx.serialize("data", event, generator);
        generator.writeEnd();
    }

}
