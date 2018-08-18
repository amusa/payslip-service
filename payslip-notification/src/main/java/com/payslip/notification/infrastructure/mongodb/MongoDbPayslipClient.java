/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.notification.infrastructure.mongodb;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.payslip.common.events.PayslipResponse;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class MongoDbPayslipClient {

    private static final Logger logger = Logger.getLogger(MongoDbPayslipClient.class.getName());

    @Inject
    MongoDatabase mongoDb;

    public PayslipResponse getPayslipResponse(String id) {
        Document payslipDoc = payslipCollection().find(eq("_id", new ObjectId(id))).first();

        String payslipJson = com.mongodb.util.JSON.serialize(payslipDoc);

        Gson gson = new Gson();
        PayslipResponse payResponse = gson.fromJson(payslipJson, PayslipResponse.class);

        logger.log(Level.INFO, "--- payslip returned and converted from db successfully ---");

        return payResponse;

    }

    private MongoCollection<Document> payslipCollection() {
        return mongoDb.getCollection("responses");
    }

}
