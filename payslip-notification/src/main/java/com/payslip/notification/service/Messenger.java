/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.notification.service;

import com.payslip.common.events.Notification;
import com.payslip.common.events.PayslipGenerated;

/**
 *
 * @author maliska
 */
public interface Messenger {

    void when(PayslipGenerated request);

    void when(Notification event);
    
}
