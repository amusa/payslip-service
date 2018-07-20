/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment;

import com.payslip.fulfilment.infrastructure.jco.PayData;
import com.sap.conn.jco.JCoException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author maliska
 */
public interface PayslipService {

    List<PayData> getPayslipBytes(String staffId, Date dateFrom, Date dateTo) throws JCoException;
}
