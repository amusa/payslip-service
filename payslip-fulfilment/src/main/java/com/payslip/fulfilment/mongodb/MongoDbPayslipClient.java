/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment.mongodb;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.payslip.common.events.PayslipResponse;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class MongoDbPayslipClient {
    
    private static final Logger logger = Logger.getLogger(MongoDbPayslipClient.class.getName());
    
    @Inject
    MongoDatabase mongoDb;
    
    public String putPayslip(PayslipResponse payslip) {
        Gson gson = new Gson();
        String payslipJson = gson.toJson(payslip);
        
        Document payslipDoc = Document.parse(payslipJson);
                
        payslipCollection().insertOne(payslipDoc);
        logger.log(Level.FINE, "--- payslip inserted to monogodb successfully ---");
        
        return payslipDoc.getObjectId("_id").toString();
        
    }
    
    private MongoCollection<Document> payslipCollection() {
        return mongoDb.getCollection("payslips");
    }
    
}
