/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.lib.common.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public Date getDate(MonthMarker marker) {
        LocalDate payDate;
        int yyyy = Integer.parseInt(year);
        int mm = Integer.parseInt(month);

        if (marker == MonthMarker.END) {
            payDate = YearMonth.of(yyyy, mm).atEndOfMonth();
        } else {
            payDate = YearMonth.of(yyyy, mm).atDay(1);
        }

        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd").parse(payDate.toString());
            return d;
        } catch (ParseException ex) {
            Logger.getLogger(PayPeriod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Date getDate() {
        return getDate(MonthMarker.BEGIN);
    }
}
