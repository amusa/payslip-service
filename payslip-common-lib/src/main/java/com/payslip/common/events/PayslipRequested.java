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
public class PayslipRequested extends AppEvent {

    private static final Logger logger = Logger.getLogger(PayslipRequested.class.getName());

    private String emailFrom;
    private Date dateSent;
    //private String staffId;
    private PayPeriod periodFrom;
    private PayPeriod periodTo;

    public PayslipRequested() {
    }
        
    public PayslipRequested(String emailFrom, String dateSent, String subject, PayPeriod periodFrom, PayPeriod periodTo) {
        super(subject);

        logger.log(Level.INFO, "emailFrom={0}, dateSent={1}, subject={2} periodFrom={3}, periodTo={4}",
                new Object[]{emailFrom, dateSent, subject, periodFrom, periodTo});

        this.emailFrom = emailFrom;
        this.dateSent = toDate(dateSent, "yyyy-MM-dd");
       // this.staffId = staffId;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
    }

    public PayslipRequested(JsonObject jsonObject) {
        this(jsonObject.getString("emailFrom"),
                jsonObject.getString("dateSent"),                
                jsonObject.getString("subject"),
                new PayPeriod(jsonObject.getJsonObject("periodFrom")),
                new PayPeriod(jsonObject.getJsonObject("periodTo"))
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

//    public String getStaffId() {
//        return staffId;
//    }
//
//    public void setStaffId(String staffId) {
//        this.staffId = staffId;
//    }

    public PayPeriod getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(PayPeriod periodFrom) {
        this.periodFrom = periodFrom;
    }

    public PayPeriod getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(PayPeriod periodTo) {
        this.periodTo = periodTo;
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
