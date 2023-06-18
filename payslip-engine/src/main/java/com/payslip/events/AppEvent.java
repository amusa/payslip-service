/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.events;

import java.time.Instant;

/**
 *
 * @author maliska
 */
public abstract class AppEvent{
    public String id;
    public Instant instant;
    public String subject;
}
