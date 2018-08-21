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
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.payslip.common.events.Notification;
import com.payslip.common.events.PayslipResponse;
import java.util.ArrayList;
import java.util.List;
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

        String payslipJson = payslipDoc.toJson();

        Gson gson = new Gson();
        PayslipResponse payResponse = gson.fromJson(payslipJson, PayslipResponse.class);

        logger.log(Level.INFO, "--- payslip returned and converted from db successfully ---");

        return payResponse;

    }

    public void deletePayslip(String id) {
        payslipCollection().deleteOne(eq("requestId", id));
    }

    public void deleteNotice(String id) {
        noticeCollection().deleteOne(eq("id", id));
    }

    public long markAsFailed(String id) {
        return payslipCollection().updateOne(
                eq("requestId", id),
                Updates.set("status", "FAILED"),
                new UpdateOptions().upsert(true)).getModifiedCount();
    }

    public List<PayslipResponse> getPayslipsForRetry(String status) {
        List<Document> payslipDocs = new ArrayList<>();
        List<PayslipResponse> payslips = new ArrayList<>();

        payslipCollection().find(eq("status", status)).into(payslipDocs);
        Gson gson = new Gson();

        for (Document doc : payslipDocs) {

            PayslipResponse payResponse = gson.fromJson(doc.toJson(), PayslipResponse.class);
            payslips.add(payResponse);

        }

        logger.log(Level.INFO, "--- payslips retrieved for retry successfully ---");

        return payslips;

    }

    public List<Notification> getNoticesForRetry() {
        List<Document> noticeDocs = new ArrayList<>();
        List<Notification> notices = new ArrayList<>();

        noticeCollection().find().into(noticeDocs);
        Gson gson = new Gson();

        for (Document doc : noticeDocs) {

            Notification notice = gson.fromJson(doc.toJson(), Notification.class);
            notices.add(notice);

        }

        logger.log(Level.INFO, "--- notices retrieved for retry successfully ---");

        return notices;

    }

    public String putNoticeForRetry(Notification notice) {
        Gson gson = new Gson();
        String noticeJson = gson.toJson(notice);

        Document noticeDoc = Document.parse(noticeJson);

        logger.log(Level.INFO, "--- inserting notice to monogodb ---");
        noticeCollection().insertOne(noticeDoc);
        logger.log(Level.INFO, "--- notice inserted to monogodb successfully ---");

        return noticeDoc.getObjectId("_id").toString();

    }

    private MongoCollection<Document> payslipCollection() {
        return mongoDb.getCollection("payslips");
    }

    private MongoCollection<Document> noticeCollection() {
        return mongoDb.getCollection("notices");
    }

}
