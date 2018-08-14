/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.kafka.deserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.payslip.common.events.AppEvent;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.common.serialization.Deserializer;

/**
 *
 * @author maliska
 */
public class AppEventJsonDeserializer implements Deserializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(AppEventJsonDeserializer.class.getName());

    @Override
    public void configure(Map<String, ?> map, boolean bln) {
    }

    @Override
    public AppEvent deserialize(String string, byte[] bytes) {
        logger.log(Level.INFO, "--- configuring custom deserialier ---");
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(AppEvent.class, new AppEventDeserializerAdapter());
        Gson gson = gsonBilder.create();

        if (bytes == null) {
            return null;
        }
        
        Type appEventType = new TypeToken<AppEvent>() {
        }.getType();

        logger.log(Level.INFO, "--- deserializing bytes ---");
        return gson.fromJson(new String(bytes), appEventType);
    }

    @Override
    public void close() {
    }

}
