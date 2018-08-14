/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.events;

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

    private int year;
    private int month;

    public PayPeriod() {
    }

    public PayPeriod(int year, int month) {
        this.year = rightSize(year);
        this.month = month;
    }

    public PayPeriod(JsonObject jsonObject) {
        this(jsonObject.getInt("year"),
                jsonObject.getInt("month")
        );
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public Date getDate(MonthMarker marker) {
        LocalDate payDate;

        if (marker == MonthMarker.END) {
            payDate = YearMonth.of(year, month).atEndOfMonth();
        } else {
            payDate = YearMonth.of(year, month).atDay(1);
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

    private int rightSize(int year) {
        return (year % 2000) + 2000;
    }
}
