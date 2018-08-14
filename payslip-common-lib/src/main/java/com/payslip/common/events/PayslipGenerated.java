/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.events;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 *
 * @author maliska
 */
public class PayslipGenerated extends AppEvent {

    private static final Logger logger = Logger.getLogger(PayslipGenerated.class.getName());

    private String emailFrom;
    private Date dateSent;
    private String staffId;
    private String requestId;
    private List<Payload> payloads;

    public PayslipGenerated() {
    }

    public PayslipGenerated(String emailFrom, Date dateSent, String staffId, String requestId, String subject, List<Payload> payloads) {
        super(subject);
        this.emailFrom = emailFrom;
        this.dateSent = dateSent;
        this.staffId = staffId;
        this.requestId = requestId;
        this.payloads = payloads;
    }

    public PayslipGenerated(String emailFrom, String dateSent, String staffId, String requestId, String subject, JsonArray payloads) {
        super(subject);

        logger.log(Level.INFO, "emailFrom={0}, dateSent={1}, staffId={2}, requestId={3}, subject={4}, payloads={5}",
                new Object[]{emailFrom, dateSent, staffId, requestId, subject, payloads});

        this.emailFrom = emailFrom;
        this.dateSent = toDate(dateSent, "yyyy-MM-dd");
        this.staffId = staffId;
        this.requestId = requestId;
        this.payloads = reconstructPayload(payloads);
//        Jsonb jsonb = JsonbBuilder.create();
//        this.payloads = jsonb.fromJson(payloads.toString(), new ArrayList() {
//        }.getClass());
    }

    public PayslipGenerated(JsonObject jsonObject) {
        this(jsonObject.getString("emailFrom"),
                jsonObject.getString("dateSent"),
                jsonObject.getString("staffId"),
                jsonObject.getString("requestId"),
                jsonObject.getString("subject"),
                jsonObject.getJsonArray("payloads")
        );
    }

    public List<Payload> reconstructPayload(JsonArray jsonArray) {
        logger.log(Level.INFO, "--- reconstructing payload from JsonArray: {0} ---", jsonArray);

        List<Payload> payload = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); ++i) {
            final JsonObject jo = jsonArray.getJsonObject(i);
            String fileName = jo.getString("pdfFileName");
            // byte[] pdf;
//            try (ByteArrayOutputStream oos = new ByteArrayOutputStream();
//                    JsonWriter writer = Json.createWriter(oos)) {
//                for()
//                writer.writeObject(jo.getJsonArray("payslipPdf"));
//                writer.close();
//                oos.flush();
//                pdf = oos.toByteArray();
//            } catch (IOException ex) {
//                Logger.getLogger(PayslipGenerated.class.getName()).log(Level.SEVERE, null, ex);
//                throw new RuntimeException("Unable to convert byte");
//            }
            byte[] pdf = jo.getJsonArray("payslipPdf").toArray().toString().getBytes();

            final Payload pl = new Payload(pdf, fileName);
            payload.add(pl);
        }

        return payload;

    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<Payload> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<Payload> payloads) {
        this.payloads = payloads;
    }

    private Date toDate(String dateStr, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);//("dd/MM/yyy");
        try {
            return sdf.parse(dateStr);
        } catch (ParseException ex) {
            Logger.getLogger(PayslipRequested.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
