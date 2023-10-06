package com.payslip.subscription;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.graph.models.Subscription;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Startup
public class Bootstrapper {

    @Inject
    private SubscriptionManager subscriptionManager;

    @PostConstruct
    public void startup() throws JsonProcessingException {
        Log.info("***Starting GraphStarter***");

        Log.info("***Checking subscription existence***");
        Optional<Subscription> subscriptionOpt = subscriptionManager.getExistingSubscription();
        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();
            Log.infov(
                    "***Subscription found***\n\tSubscription ID: {0}\n\tChange Notification URL: {1}\n\tLifecycle Notification URL: {2}\n\tSubscription Expiration Duration: {3} mins",
                    subscription.id, subscription.notificationUrl, subscription.lifecycleNotificationUrl,
                    ChronoUnit.MINUTES.between(OffsetDateTime.now(), subscription.expirationDateTime));

            if (ChronoUnit.MINUTES.between(OffsetDateTime.now(),
                    subscription.expirationDateTime) < 15) {
                Log.infov("***Re-subscribing {0} ***", subscription.id);
                subscriptionManager.resubscribe(subscription.id);
            }

        } else {
            Log.infov("***No subscriptions found***");
            subscriptionManager.subscribe();
        }

        // Log.infov("***Processing delta messages***");
        // subscriptionManager.processDeltaMessages();

    }
}
