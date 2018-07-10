package com.payslip.api.infrastructure.kafka;

import com.payslip.lib.common.events.PayslipRequested;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;



public class EventJsonbSerializer implements JsonbSerializer<PayslipRequested> {

    @Override
    public void serialize(final PayslipRequested event, final JsonGenerator generator, final SerializationContext ctx) {
        generator.write("class", event.getClass().getCanonicalName());
        generator.writeStartObject("data");
        ctx.serialize("data", event, generator);
        generator.writeEnd();
    }

}
