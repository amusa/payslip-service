/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.events;

import java.time.LocalDateTime;

/**
 *
 * @author maliska
 */
public class PayslipRequested extends AppEvent {

    public String emailFrom;
    public LocalDateTime dateSent;
    // private String staffId;
    public PayPeriod periodFrom;
    public PayPeriod periodTo;

    public PayslipRequested() {
    }

    // public PayslipRequested(String emailFrom, String dateSent, String subject, PayPeriod periodFrom,
    //         PayPeriod periodTo) {
    //     //super(subject);

    //     Log.infov("emailFrom={0}, dateSent={1}, subject={2} periodFrom={3}, periodTo={4}",
    //             new Object[] { emailFrom, dateSent, subject, periodFrom, periodTo });

    //     this.emailFrom = emailFrom;
    //     this.dateSent = toDate(dateSent, "yyyy-MM-dd HH:mm");
    //     this.periodFrom = periodFrom;
    //     this.periodTo = periodTo;
    // }

    // public PayslipRequested(JsonObject jsonObject) {
    // this(jsonObject.getString("emailFrom"),
    // jsonObject.getString("dateSent"),
    // jsonObject.getString("subject"),
    // new PayPeriod(jsonObject.getJsonObject("periodFrom")),
    // new PayPeriod(jsonObject.getJsonObject("periodTo")));
    // }

    // public String getEmailFrom() {
    //     return emailFrom;
    // }

    // public void setEmailFrom(String emailFrom) {
    //     this.emailFrom = emailFrom;
    // }

    // public LocalDateTime getDateSent() {
    //     return dateSent;
    // }

    // public void setDateSent(LocalDateTime dateSent) {
    //     this.dateSent = dateSent;
    // }

    // public String getStaffId() {
    // return staffId;
    // }
    //
    // public void setStaffId(String staffId) {
    // this.staffId = staffId;
    // }

    // public PayPeriod getPeriodFrom() {
    //     return periodFrom;
    // }

    // public void setPeriodFrom(PayPeriod periodFrom) {
    //     this.periodFrom = periodFrom;
    // }

    // public PayPeriod getPeriodTo() {
    //     return periodTo;
    // }

    // public void setPeriodTo(PayPeriod periodTo) {
    //     this.periodTo = periodTo;
    // }

    // private LocalDateTime toDate(String dateStr, String pattern) {        
    //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

    //     // try {
    //     return LocalDateTime.parse(dateStr, formatter);
        
    // }

}
