/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.events;

import java.time.LocalDateTime;

/**
 *
 * @author maliska
 */
public class PayslipRequested extends AppEvent {

    public String emailFrom;
    public LocalDateTime dateSent;
    public PayPeriod periodFrom;
    public PayPeriod periodTo;
}
