/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.payslip.services;

import com.payslip.payslip.services.jco.PayData;
import com.sap.conn.jco.JCoException;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author maliska
 */
public interface PayslipService {

    List<PayData> getPayslipBytes(String staffId, LocalDate dateFrom, LocalDate dateTo) throws JCoException;
}
