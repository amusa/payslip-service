package com.payslip.notification.service;

import com.payslip.common.events.Notification;
import com.payslip.notification.infrastructure.mongodb.MongoDbPayslipClient;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NoticeResponseWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(NoticeResponseWorker.class.getName());
    private MongoDbPayslipClient dbClient;
    private Messenger messenger;

    public NoticeResponseWorker(MongoDbPayslipClient dbClient, Messenger messenger) {
        this.dbClient = dbClient;
        this.messenger = messenger;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "--- notice retry worker triggered ---");
        List<Notification> notices = dbClient.getNoticesForRetry();
        logger.log(Level.INFO, "--- notices fetched. size={0} ---", (notices != null ? notices.size() : 0));
        for (Notification nt : notices) {
            logger.log(Level.INFO, "--- emailing id:{0} ---", nt.getRequestId());
            messenger.mailNotice(nt, true);
        }
    }

}
