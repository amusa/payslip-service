/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.kafka.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.payslip.common.events.AppEvent;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
public class AppEventSerializerAdapter implements JsonSerializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(AppEventSerializerAdapter.class.getName());

    @Override
    public JsonElement serialize(AppEvent event, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(event.getClass().getName()));
        result.add("properties", context.serialize(event, event.getClass()));

        logger.log(Level.INFO, "--- returning JsonObject: {0} ---");
        return result;
    }

}
