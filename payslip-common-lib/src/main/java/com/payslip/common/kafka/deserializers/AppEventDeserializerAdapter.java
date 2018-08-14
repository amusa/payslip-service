/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.kafka.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.payslip.common.events.AppEvent;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
public class AppEventDeserializerAdapter implements JsonDeserializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(AppEventDeserializerAdapter.class.getName());

    @Override
    public AppEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonElement element = jsonObject.get("properties");

        logger.log(Level.INFO, "--- type:{0}, properties:{1} ---", new Object[]{type, element});

        try {
            AppEvent appEvent = context.deserialize(element, Class.forName(type));
            logger.log(Level.INFO, "--- returning deserialized AppEvent---");
            return appEvent;
        } catch (ClassNotFoundException cnfe) {
            throw new JsonParseException("Unknown element type: " + type, cnfe);
        }
    }

}
