/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
public class PayslipGenerated extends AppEvent {

    private static final Logger logger = Logger.getLogger(PayslipGenerated.class.getName());

    private String emailFrom;
    private Date dateSent;
    //private String staffId;
    private String requestId;
    private String referenceId;
    //private List<Payload> payloads;

    public PayslipGenerated() {
    }

    public PayslipGenerated(String emailFrom, Date dateSent, String requestId, String subject,
    //, List<Payload> payloads
            String refId
    ) {
        super(subject);
        this.emailFrom = emailFrom;
        this.dateSent = dateSent;
        //this.staffId = staffId;
        this.requestId = requestId;
        // this.payloads = payloads;
        this.referenceId = refId;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

//    public List<Payload> getPayloads() {
//        return payloads;
//    }
//
//    public void setPayloads(List<Payload> payloads) {
//        this.payloads = payloads;
//    }
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    private Date toDate(String dateStr, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException ex) {
            Logger.getLogger(PayslipRequested.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
