/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.lib.common.events;

import javax.json.JsonObject;

/**
 *
 * @author maliska
 */
public class PayPeriod {

    private String year;
    private String month;

    public PayPeriod() {
    }

    public PayPeriod(String year, String month) {
        this.year = year;
        this.month = month;
    }

    public PayPeriod(JsonObject jsonObject) {
        this(jsonObject.getString("year"),
                jsonObject.getString("month")
        );
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

}
