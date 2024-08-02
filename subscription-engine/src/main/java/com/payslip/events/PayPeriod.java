/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.events;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
// import javax.json.JsonObject;

/**
 *
 * @author maliska
 */
public class PayPeriod implements Serializable{

    private int year;
    private int month;

    public PayPeriod() {
    }

    public PayPeriod(int year, int month) {
        this.year = rightSize(year);
        this.month = month;
    }

    // public PayPeriod(JsonObject jsonObject) {
    //     this(jsonObject.getInt("year"),
    //             jsonObject.getInt("month"));
    // }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public LocalDate getDate(MonthMarker marker) {
        LocalDate payDate;

        if (marker == MonthMarker.END) {
            payDate = YearMonth.of(year, month).atEndOfMonth();
        } else {
            payDate = YearMonth.of(year, month).atDay(1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(payDate.toString(), formatter);

        // try {
        // java.util.Date d = new
        // SimpleDateFormat("yyyy-MM-dd").parse(payDate.toString());
        // return d;
        // } catch (ParseException ex) {
        // Logger.getLogger(PayPeriod.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return null;
    }

    public LocalDate getDate() {
        return getDate(MonthMarker.BEGIN);
    }

    private int rightSize(int year) {
        return (year % 2000) + 2000;
    }
}
