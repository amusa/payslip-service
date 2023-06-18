/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.util;

import io.quarkus.logging.Log;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.payslip.events.PayPeriod;
import com.payslip.events.PayslipRequested;

/**
 *
 * @author maliska
 */
public class RequestParser {

    public static PayslipRequested parse(String emailSubject, LocalDateTime dateSent, String emailFrom)
            throws ParseException {
        String[] tokens = emailSubject.split("\\s+");
        String dateFrom = null;
        String dateTo = null;

        PayslipRequested request = new PayslipRequested();
            
        request.id=UUID.randomUUID().toString();
        request.instant=Instant.now();
        request.subject = emailSubject;

        Log.info("--- parsing payslip request ---");

        if (tokens[0].toUpperCase().matches("#PAYSLIP")) {
            if (tokens.length > 1 && tokens[1].matches("\\d{1,2}[/|-]\\d{2,4}")) {
                dateFrom = tokens[1];
                Log.infov("--- 1st date extracted: {0} ---", dateFrom);

                String periodFrom[] = dateFrom.split("[/|-]");

                PayPeriod fP = getPayPeriod(periodFrom[1], periodFrom[0]);
                request.periodFrom = fP;

                if (tokens.length > 2 && tokens[2].matches("\\d{1,2}[/|-]\\d{2,4}")) {
                    dateTo = tokens[2];
                    Log.tracev("--- 2nd date extracted: {0} ---", dateTo);
                }

                if (null == dateTo) {
                    Log.tracev("--- setting payDateTo to payDateFrom: {0} ---", fP);
                    request.periodTo = fP;
                } else {
                    String periodTo[] = dateTo.split("[/|-]");
                    request.periodTo = getPayPeriod(periodTo[1], periodTo[0]);
                }

            } else {
                LocalDate ld = LocalDate.now();
                PayPeriod currentPeriod = new PayPeriod(ld.getYear(), ld.getMonthValue());
                request.periodFrom = currentPeriod;
                request.periodTo = currentPeriod;
            }

            Log.tracev("--- setting dateSent: {0} ---", dateSent);
            request.dateSent = dateSent;
            Log.tracev("--- setting emailFrom: {0} ---", emailFrom);
            request.emailFrom = emailFrom;

            return request;
        }
        throw new RuntimeException("Invalid Payslip request. Syntax: #PAYSLIP MM/YY or #PAYSLIP MM/YY MM/YY");
    }

    private static PayPeriod getPayPeriod(String year, String month) {
        Log.tracev("--- generating pay period: year={0}, month={1} ---", new Object[] { year, month });
        if (null != year && null != month) {
            return new PayPeriod(Integer.parseInt(year), Integer.parseInt(month));
        }
        return null;
    }

}
