/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api;

import com.payslip.lib.common.events.PayPeriod;
import com.payslip.lib.common.events.PayslipRequested;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author maliska
 */
public class RequestParser {

    public static PayslipRequested parse(String emailSubject, Date dateSent, String emailFrom) throws ParseException {        
        String[] tokens = emailSubject.split("\\s");
        String staffId = null;
        String dateFrom = null;
        String dateTo = null;

        if (tokens[0].matches("#PAYSLIP")) {
            staffId = tokens[1];

            if (tokens[2].matches("\\d{2}/\\d{4}")) {
                dateFrom = tokens[2];
            }

            if (tokens[3].matches("\\d{2}/\\d{4}")) {
                dateTo = tokens[3];
            }
        }

        PayslipRequested request = new PayslipRequested();

        request.setStaffId(staffId);
        String periodFrom[] = dateFrom.split("/");
        request.setPeriodFrom(new PayPeriod(periodFrom[1], periodFrom[0]));
        String periodTo[] = dateTo.split("/");
        request.setPeriodTo(new PayPeriod(periodTo[1], periodTo[0]));
        request.setDateSent(dateSent);
        request.setEmailFrom(emailFrom);

        return request;

    }

}
