/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.service;


import com.payslip.lib.common.events.PayslipRequested;
import java.util.List;

/**
 *
 * @author maliska
 */
public interface RequestProcessor {

    public List<PayslipRequested> pullEmailRequests() throws Exception;
}
