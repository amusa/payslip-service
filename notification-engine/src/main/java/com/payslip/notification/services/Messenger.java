/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.notification.services;

import com.payslip.events.Notification;
import com.payslip.events.PayslipGenerated;
;

/**
 *
 * @author maliska
 */
public interface Messenger {    
    void send(PayslipGenerated payslip, boolean retry);
    void send(Notification notice, boolean retry);
}
