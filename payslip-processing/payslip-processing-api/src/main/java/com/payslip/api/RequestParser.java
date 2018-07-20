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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
public class RequestParser {

    private static final Logger logger = Logger.getLogger(RequestParser.class.getName());

    public static PayslipRequested parse(String emailSubject, Date dateSent, String emailFrom) throws ParseException {
        String[] tokens = emailSubject.split("\\s");
        String staffId = null;
        String dateFrom = null;
        String dateTo = null;

        logger.log(Level.INFO, "--- parsing ---");

        if (tokens.length < 3) {
            throw new RuntimeException("Wrong request format");
        }

        logger.log(Level.INFO, "--- minimum tokens met ---");

        if (tokens[0].matches("#PAYSLIP")) {
            staffId = tokens[1];

            logger.log(Level.INFO, "--- extracted staff id: {0} ---", staffId);

            if (tokens[2].matches("\\d{1,2}/\\d{4}")) {
                dateFrom = tokens[2];
                logger.log(Level.INFO, "--- 1st date extracted: {0} ---", dateFrom);
            }

            if (tokens.length > 3 && tokens[3].matches("\\d{1,2}/\\d{4}")) {
                dateTo = tokens[3];
                logger.log(Level.INFO, "--- 2nd date extracted: {0} ---", dateTo);
            }
        }

        PayslipRequested request = new PayslipRequested();

        request.setStaffId(staffId);

        if (null == dateFrom) {
            throw new RuntimeException("No pay period specified");
        }

        String periodFrom[] = dateFrom.split("/");

        PayPeriod fP = getPayPeriod(periodFrom[1], periodFrom[0]);
        request.setPeriodFrom(fP);

        if (null == dateTo) {
            logger.log(Level.INFO, "--- setting payDateTo to payDateFrom: {0} ---", fP);
            request.setPeriodTo(fP);
        } else {
            String periodTo[] = dateTo.split("/");
            request.setPeriodTo(getPayPeriod(periodTo[1], periodTo[0]));
        }
        logger.log(Level.INFO, "--- setting dateSent: {0} ---", dateSent);
        request.setDateSent(dateSent);
        logger.log(Level.INFO, "--- setting emailFrom: {0} ---", emailFrom);
        request.setEmailFrom(emailFrom);

        return request;

    }

    private static PayPeriod getPayPeriod(String year, String month) {
        logger.log(Level.INFO, "--- generating pay period: year={0}, month={1} ---", new Object[]{year, month});
        if (null != year && null != month) {
            return new PayPeriod(Integer.parseInt(year), Integer.parseInt(month));
        }
        return null;
    }

}
