/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.lib.common.events;

import com.google.gson.Gson;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;

/**
 *
 * @author maliska
 */
public class PayslipRequested implements Serializable {

    private String emailFrom;

    private Date dateSent;

    private String staffId;

    private PayPeriod periodFrom;

    private PayPeriod periodTo;

    public PayslipRequested() {
    }

    public PayslipRequested(String emailFrom, String dateSent, String staffId, PayPeriod periodFrom, PayPeriod periodTo) {
        this.emailFrom = emailFrom;
        this.dateSent = toDate(dateSent);
        this.staffId = staffId;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
    }

    public PayslipRequested(JsonObject jsonObject) {
        this(jsonObject.getString("emailFrom"),
                jsonObject.getString("dateSent"),
                jsonObject.getString("staffId"),
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

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

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

    private Date toDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
        try {
            return sdf.parse(dateStr);
        } catch (ParseException ex) {
            Logger.getLogger(PayslipRequested.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.emailFrom);
        hash = 29 * hash + Objects.hashCode(this.dateSent);
        hash = 29 * hash + Objects.hashCode(this.staffId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PayslipRequested other = (PayslipRequested) obj;
        if (!Objects.equals(this.emailFrom, other.emailFrom)) {
            return false;
        }
        if (!Objects.equals(this.staffId, other.staffId)) {
            return false;
        }
        if (!Objects.equals(this.dateSent, other.dateSent)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
