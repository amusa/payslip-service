package com.payslip.health;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import com.microsoft.graph.models.Subscription;
import com.payslip.subscription.SubscriptionManager;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Liveness
@ApplicationScoped
public class SubscriptionHealthCheck implements HealthCheck {

    @Inject
    private SubscriptionManager subscriptionManager;

    @Override
    public HealthCheckResponse call() {
        Log.trace("***subscription liveness probe triggered***");
        Optional<Subscription> subscriptionOpt = subscriptionManager.getExistingSubscription();
        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();            

            return HealthCheckResponse.named("Subscription health check")
                    .up()
                    .withData("subscriptionId", subscription.id)
                    .withData("Change Notification URL", subscription.notificationUrl)
                    .withData("Lifecycle Notification URL", subscription.lifecycleNotificationUrl)
                    .withData("Subscription Expiration Duration", ChronoUnit.MINUTES.between(OffsetDateTime.now(), subscription.expirationDateTime))
                    .build();

        }

        return HealthCheckResponse.up("Subscription health check");

    }

}
