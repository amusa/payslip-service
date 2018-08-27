/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.util;

import com.payslip.common.events.PayPeriod;
import com.payslip.common.events.PayslipRequested;
import java.text.ParseException;
import java.time.LocalDate;
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
        String[] tokens = emailSubject.split("\\s+");
        //String staffId = null;
        String dateFrom = null;
        String dateTo = null;

        PayslipRequested request = new PayslipRequested();
        request.setSubject(emailSubject);

        logger.log(Level.INFO, "--- parsing ---");

        if (tokens[0].toUpperCase().matches("#PAYSLIP")) {
            if (tokens.length > 1 && tokens[1].matches("\\d{1,2}[/|-]\\d{2,4}")) {
                dateFrom = tokens[1];
                logger.log(Level.INFO, "--- 1st date extracted: {0} ---", dateFrom);

                String periodFrom[] = dateFrom.split("[/|-]");

                PayPeriod fP = getPayPeriod(periodFrom[1], periodFrom[0]);
                request.setPeriodFrom(fP);

                if (tokens.length > 2 && tokens[2].matches("\\d{1,2}[/|-]\\d{2,4}")) {
                    dateTo = tokens[2];
                    logger.log(Level.INFO, "--- 2nd date extracted: {0} ---", dateTo);
                }

                if (null == dateTo) {
                    logger.log(Level.INFO, "--- setting payDateTo to payDateFrom: {0} ---", fP);
                    request.setPeriodTo(fP);
                } else {
                    String periodTo[] = dateTo.split("[/|-]");
                    request.setPeriodTo(getPayPeriod(periodTo[1], periodTo[0]));
                }

            } else {
                LocalDate ld = LocalDate.now();
                PayPeriod currentPeriod = new PayPeriod(ld.getYear(), ld.getMonthValue());
                request.setPeriodFrom(currentPeriod);
                request.setPeriodTo(currentPeriod);
            }

            logger.log(Level.INFO, "--- setting dateSent: {0} ---", dateSent);
            request.setDateSent(dateSent);
            logger.log(Level.INFO, "--- setting emailFrom: {0} ---", emailFrom);
            request.setEmailFrom(emailFrom);

            return request;
        }
        throw new RuntimeException("Not payslip request");
    }

//    public static PayslipRequested parse2(String emailSubject, Date dateSent, String emailFrom) throws ParseException {
//        String[] tokens = emailSubject.split("\\s");
//        String staffId = null;
//        String dateFrom = null;
//        String dateTo = null;
//
//        logger.log(Level.INFO, "--- parsing ---");
//
//        Pattern pattern = Pattern.compile("(\\d{5,10})(\\d{1,2}/\\d{4})(\\d{1,2}/\\d{4})");
//        Matcher matcher = pattern.matcher(emailSubject);
//
//        if (matcher.matches()) {
//            logger.log(Level.INFO, "--- pattern matches ---");
//            staffId = matcher.group(0);
//            logger.log(Level.INFO, "--- group(0)={0} ---", matcher.group(0));
//            logger.log(Level.INFO, "--- extracted staff id: {0} ---", staffId);
//            logger.log(Level.INFO, "--- group(1)={0} ---", matcher.group(1));
//            dateFrom = matcher.group(1);
//            logger.log(Level.INFO, "--- group(2)={0} ---", matcher.group(2));
//            dateTo = matcher.group(2);
//
//            PayslipRequested request = new PayslipRequested();
//
//            request.setStaffId(staffId);
//
//            if (null == dateFrom) {
//                throw new RuntimeException("No pay period specified");
//            }
//
//            String periodFrom[] = dateFrom.split("/");
//
//            PayPeriod fP = getPayPeriod(periodFrom[1], periodFrom[0]);
//
//            request.setPeriodFrom(fP);
//
//            if (null == dateTo) {
//                logger.log(Level.INFO, "--- setting payDateTo to payDateFrom: {0} ---", fP);
//                request.setPeriodTo(fP);
//            } else {
//                String periodTo[] = dateTo.split("/");
//                request.setPeriodTo(getPayPeriod(periodTo[1], periodTo[0]));
//            }
//
//            logger.log(Level.INFO,
//                    "--- setting dateSent: {0} ---", dateSent);
//            request.setDateSent(dateSent);
//
//            logger.log(Level.INFO,
//                    "--- setting emailFrom: {0} ---", emailFrom);
//            request.setEmailFrom(emailFrom);
//
//            return request;
//        }
//
//        return null;
//    }
    private static PayPeriod getPayPeriod(String year, String month) {
        logger.log(Level.INFO, "--- generating pay period: year={0}, month={1} ---", new Object[]{year, month});
        if (null != year && null != month) {
            return new PayPeriod(Integer.parseInt(year), Integer.parseInt(month));
        }
        return null;
    }

}
