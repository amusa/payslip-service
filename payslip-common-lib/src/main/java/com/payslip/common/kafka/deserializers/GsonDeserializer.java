/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.kafka.deserializers;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author maliska
 */
public class GsonDeserializer<T> implements Deserializer<T> {

    private static final Logger logger = Logger.getLogger(GsonDeserializer.class.getName());

    private Gson gson = new Gson();
    private Class<T> deserializedClass;

    public GsonDeserializer(Class<T> deserializedClass) {
        this.deserializedClass = deserializedClass;
    }

    public GsonDeserializer() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> map, boolean b) {
        if (deserializedClass == null) {           
            deserializedClass = (Class<T>) map.get("serializedClass");
             logger.log(Level.INFO, "--- deserializedClass is null. serializedClass={0} ---", deserializedClass);
        }
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return gson.fromJson(new String(bytes), deserializedClass);

    }

    @Override
    public void close() {

    }
}
