/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.kafka.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.payslip.common.events.AppEvent;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.common.serialization.Serializer;

/**
 *
 * @author maliska
 */
public class AppEventJsonSerializer implements Serializer<AppEvent> {

    private static final Logger logger = Logger.getLogger(AppEventJsonSerializer.class.getName());

    @Override
    public void configure(Map<String, ?> map, boolean bln) {

    }

    @Override
    public byte[] serialize(String string, AppEvent event) {
        logger.log(Level.INFO, "--- configuring custom serializer ---");
       
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(AppEvent.class, new AppEventSerializerAdapter())
                .create();

        logger.log(Level.INFO, "--- returning bytes ---");

        Type appEventType = new TypeToken<AppEvent>() {
        }.getType();
        
        return gson.toJson(event, appEventType).getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public void close() {

    }

}
