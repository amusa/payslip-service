/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.events;

import com.google.gson.Gson;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;

/**
 *
 * @author maliska
 */
public class Notification extends AppEvent {

    private static final Logger logger = Logger.getLogger(Notification.class.getName());

    private String emailFrom;
    private Date dateSent;
    private String message;    
    private String requestId;

    public Notification() {
    }

    public Notification(String emailFrom, Date dateSent, String requestId, String subject, String message) {
        super(subject);
        this.emailFrom = emailFrom;
        this.dateSent = dateSent;       
        this.message = message;
        this.requestId = requestId;

    }

    public Notification(String emailFrom, String dateSent, String requestId, String subject, String message) {
        super(subject);
        this.emailFrom = emailFrom;
        this.dateSent = toDate(dateSent);        
        this.message = message;
        this.requestId = requestId;

        logger.log(Level.INFO, "--- constructing notification: emailFrom={0}, dateSent={1}, requestId={2}, subject={3}, message={4} ---",
                new Object[]{emailFrom, dateSent, requestId, subject, message});
    }

    public Notification(JsonObject jsonObject) {
        this(jsonObject.getString("emailFrom"),
                jsonObject.getString("dateSent"),            
                jsonObject.getString("requestId"),
                jsonObject.getString("subject"),
                jsonObject.getString("message")
        );
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    private Date toDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(dateStr);
        } catch (ParseException ex) {
            Logger.getLogger(PayslipRequested.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
}
