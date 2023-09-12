package com.payslip.subscription;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.microsoft.graph.models.ChangeType;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageDeltaCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionPage;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.Request;

@ApplicationScoped
public class SubscriptionManager {
    @ConfigProperty(name = "app.notificationHost")
    String notificationHost;

    @ConfigProperty(name = "app.lifecycleHost")
    String lifecycleHost;

    @ConfigProperty(name = "app.redirectUri")
    String redirectUri;

    @ConfigProperty(name = "app.userId")
    String userId;

    private static final String APP_ONLY = "APP-ONLY";

    @Inject
    private CertificateStoreService certificateStore;

    @Inject
    private SubscriptionStoreService subscriptionStore;

    @Inject
    private GraphClientProvider graphClientProvider;

    ObjectMapper mapper;

    {
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    public void subscribe() {

        final GraphServiceClient<Request> graphClient = graphClientProvider.getGraphClient();

        Log.infov("***checking multiple subscriptions***");

        // Apps are only allowed one subscription to the /teams/getAllMessages resource
        // If we already had one, delete it so we can create a new one
        // final var existingSubscriptions =
        // subscriptionStore.getSubscriptionsForUser(APP_ONLY);
        // for (final var sub : existingSubscriptions) {
        // Log.infov("***Deleting subscription: {0}***", sub.subscriptionId);
        // graphClient.subscriptions(sub.subscriptionId).buildRequest().delete();
        // }

        // Create the subscription
        final var subscriptionRequest = new Subscription();
        subscriptionRequest.changeType = ChangeType.CREATED.toString();
        subscriptionRequest.notificationUrl = notificationHost + "/api/notification/listen";
        subscriptionRequest.lifecycleNotificationUrl = lifecycleHost + "/api/lifecycle";
        subscriptionRequest.resource = "users/" + userId
                + "/mailfolders('inbox')/messages?$select=id,createdDateTime,sender,toRecipients,sentDateTime,Subject,receivedDateTime,from,unsubscribeData";
        subscriptionRequest.clientState = UUID.randomUUID().toString();
        subscriptionRequest.includeResourceData = true;
        subscriptionRequest.encryptionCertificate = certificateStore.getBase64EncodedCertificate();
        subscriptionRequest.encryptionCertificateId = certificateStore.getCertificateId();
        subscriptionRequest.expirationDateTime = OffsetDateTime.now().plusHours(2);

        Log.infov("***Subscribing to mail notification on url: {0}***", notificationHost);

        final var subscriptionFuture = graphClient.subscriptions().buildRequest().postAsync(subscriptionRequest)
                .thenApply(subscription -> {
                    // Add record in subscription store
                    subscriptionStore.addSubscription(subscription, APP_ONLY);
                    Log.infov("***Subscription successful: {0}***", subscription);
                    return "";
                }).exceptionally(e -> {
                    Log.error("***Error occured creating subscription***\n", e, null, e);
                    return "";
                });

    }

    public Optional<Subscription> getExistingSubscription() {
        // check existing subscriptions
        final GraphServiceClient<Request> graphClient = graphClientProvider.getGraphClient();
        SubscriptionCollectionPage subscriptions = graphClient.subscriptions()
                .buildRequest()
                .get();

        if (subscriptions != null) {
            for (Subscription sub : subscriptions.getCurrentPage()) {
                return Optional.of(sub);// return first subscription and skip the rest if any
            }

        }
        return Optional.empty();

    }

    public void resubscribe(String subscriptionId) {

        final GraphServiceClient<Request> graphClient = graphClientProvider.getGraphClient();

        Subscription subscription = new Subscription();
        subscription.expirationDateTime = OffsetDateTime.now().plusHours(2);

        var subscriptionFuture = graphClient.subscriptions(subscriptionId)
                .buildRequest()
                .patchAsync(subscription);

        Log.infov("***Subscription successfully renewed***");
    }

    public void processDeltaMessages(@Nonnull String jsonPayload) throws JsonProcessingException {
        processDeltaMessages();
    }

    public void processDeltaMessages() throws JsonProcessingException {
        final GraphServiceClient<Request> graphClient = graphClientProvider.getGraphClient();

        LinkedList<Option> requestOptions = new LinkedList<Option>();
        requestOptions.add(new HeaderOption("Prefer", "odata.maxpagesize=10"));

        MessageDeltaCollectionPage delta = graphClient.users(userId).mailFolders(
                "inbox")
                .messages()
                .delta()
                .buildRequest(requestOptions)
                .get();

        for (Message message : delta.getCurrentPage()) {
            Log.infov("***Processing new message:***\n\tId:{0}, Subject:{1}, Sender:{2}, Recipient:{3}",
                    message.id, message.subject, message.from, mapper.writeValueAsString(message.toRecipients));
        }
    }
}
