// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.payslip.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.microsoft.graph.models.Subscription;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;



/**
 * Service responsible for recording all subscriptions created by the application This
 * implementation is in-memory, so all records are lost if the app is restarted
 */
@ApplicationScoped
public class SubscriptionStoreService {

    private static Map<String, SubscriptionRecord> subscriptions = new HashMap<>();


    /**
     * Adds a subscription to the store
     *
     * @param subscription the subscription to add
     * @param userId the user's ID
     */
    public void addSubscription(@Nonnull final Subscription subscription,
            @Nonnull final String userId) {
        var newRecord = new SubscriptionRecord(Objects.requireNonNull(subscription.id),
            Objects.requireNonNull(userId),
            Objects.requireNonNull(subscription.clientState));
        subscriptions.put(subscription.id, newRecord);
    }


    /**
     * Get a subscription by ID
     *
     * @param id the ID of the subscription
     * @return the subscription with the matching ID
     */
    public SubscriptionRecord getSubscription(@Nonnull final String id) {
        return subscriptions.get(Objects.requireNonNull(id));
    }


    /**
     * Delete a subscription
     *
     * @param id the ID of the subscription
     */
    public void deleteSubscription(@Nonnull final String id) {
        subscriptions.remove(Objects.requireNonNull(id));
    }


    /**
     * Get all subscriptions for a given user ID
     *
     * @param userId The user ID to match
     * @return A list of subscriptions with the specified user ID
     */
    public List<SubscriptionRecord> getSubscriptionsForUser(@Nonnull final String userId) {
        final List<SubscriptionRecord> userSubscriptions = new ArrayList<>();

        subscriptions.forEach((id, subscription) -> {
            if (subscription.userId.equals(userId)) {
                userSubscriptions.add(subscription);
            }
        });

        return userSubscriptions;
    }
}
