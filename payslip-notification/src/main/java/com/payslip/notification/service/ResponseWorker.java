package com.payslip.notification.service;

import com.payslip.common.events.PayslipResponse;
import com.payslip.notification.infrastructure.mongodb.MongoDbPayslipClient;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(ResponseWorker.class.getName());
    private MongoDbPayslipClient dbClient;
    private Messenger messenger;

    public ResponseWorker(MongoDbPayslipClient dbClient, Messenger messenger) {
        this.dbClient = dbClient;
        this.messenger = messenger;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "--- payslip retry worker triggered ---");
        List<PayslipResponse> payslips = dbClient.getPayslipsForRetry("FAILED");
        logger.log(Level.INFO, "--- payslips fetched. size={0} ---", (payslips != null ? payslips.size() : 0));
        for (PayslipResponse ps : payslips) {
            logger.log(Level.INFO, "--- mailing mail id:{0} ---", ps.getRequestId());
            messenger.mailPayslip(ps, true);
        }
    }

}
