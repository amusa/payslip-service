/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.events;

import java.time.LocalDateTime;
import java.util.List;


/**
 *
 * @author maliska
 */
public class PayslipGenerated extends AppEvent {
    public String emailFrom;
    public LocalDateTime dateSent;
    public String requestId;
    public String referenceId;
    public String body;
    public List<Payload> payloads;
}
