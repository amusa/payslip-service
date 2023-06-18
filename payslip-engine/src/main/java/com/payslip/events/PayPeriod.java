/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.events;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author maliska
 */
public class PayPeriod{

    public int year;
    public int month;

    public PayPeriod() {
    }

    public PayPeriod(int year, int month) {
        this.year = rightSize(year);
        this.month = month;
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
       
    }

    public LocalDate getDate() {
        return getDate(MonthMarker.BEGIN);
    }

    private int rightSize(int year) {
        return (year % 2000) + 2000;
    }
}
