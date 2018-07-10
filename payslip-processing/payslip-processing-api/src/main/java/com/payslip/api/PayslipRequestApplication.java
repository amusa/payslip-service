/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api;

import com.payslip.api.infrastructure.kafka.EventProducer;
import com.payslip.api.service.RequestProcessor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author maliska
 */
@Singleton
@Startup
public class PayslipRequestApplication {

    private static final Logger logger = Logger.getLogger(PayslipRequestApplication.class.getName());

    @Inject
    EventProducer producer;

    @Inject
    RequestProcessor processor;

    ScheduledExecutorService executor;

    @PostConstruct
    private void init() {
        executor = Executors.newScheduledThreadPool(3);

        Runnable task = new RequestRunnable(producer, processor);

        int initialDelay = 0;
        int period = 1;
        executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);

        logger.log(Level.INFO, "--- Payslip request processor scheduled");
    }

}
