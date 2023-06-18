package com.payslip.subscription;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.ChangeNotificationCollection;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.serializer.DefaultSerializer;
import com.payslip.util.MessageAdapter;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/notification")
public class NotificationListenerResource {

    @ConfigProperty(name = "app.clientId")
    private String clientId;

    @ConfigProperty(name = "app.tenantId")
    private String tenantId;

    @ConfigProperty(name = "app.keydiscoveryurl")
    private String keyDiscoveryUrl;

    @Inject
    private SubscriptionStoreService subscriptionStore;

    @Inject
    private CertificateStoreService certificateStore;

    @Inject
    Event<NewMessageNotification> events;

    ObjectMapper mapper;
    
    {
         mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }


    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/listen")
    // public RestResponse<String> handleValidation2(@QueryParam("validationToken")
    // String validationToken){
    public CompletableFuture<Response> handleValidation2(@QueryParam("validationToken") String validationToken) {

        Log.infov("***HANDLE VALIDATION: Validation token received: {0}***", validationToken);

       
        return CompletableFuture.completedFuture(Response.status(200).entity(validationToken).build());

    }

    @POST
    @Path("/listen")
    @Consumes(MediaType.APPLICATION_JSON)
    // @Produces(MediaType.APPLICATION_JSON)
    public CompletableFuture<Response> handleNotification(@Nonnull String jsonPayload) throws JsonProcessingException {
        Log.infov("***Handling notification***");

        // Deserialize the JSON body into a ChangeNotificationCollection
        final var serializer = new DefaultSerializer(new DefaultLogger());
        final var notifications = serializer.deserializeObject(jsonPayload, ChangeNotificationCollection.class);

        // Check for validation tokens
        boolean areTokensValid = true;
        if (notifications.validationTokens != null && !notifications.validationTokens.isEmpty()) {
            areTokensValid = TokenHelper.areValidationTokensValid(new String[] { clientId },
                    new String[] { tenantId },
                    Objects.requireNonNull(notifications.validationTokens),
                    Objects.requireNonNull(keyDiscoveryUrl));
        }

        if (areTokensValid) {
            for (ChangeNotification notification : notifications.value) {
                // Look up subscription in store
                var subscription = subscriptionStore.getSubscription(
                        Objects.requireNonNull(notification.subscriptionId.toString()));

                // Only process if we know about this subscription AND
                // the client state in the notification matches
                //if (subscription != null
                //        && subscription.clientState.equals(notification.clientState)) {
                    if (notification.encryptedContent != null) {                        
                        // With encrypted content, this is a new channel message
                        // notification with encrypted resource data
                        processNewMessageNotification(notification, subscription);
                    }
               // }
            }
        }

        return CompletableFuture.completedFuture(Response.accepted().build());
    }

    

    
// TODO: migrate function to a dedicated bean
    /**
     * Processes a new channel message notification by decrypting the included
     * resource data
     *
     * @param notification the new channel message notification
     * @param subscription the matching subscription record
     * @throws JsonProcessingException
     */
    private void processNewMessageNotification(
            @Nonnull final ChangeNotification notification,
            @Nonnull final SubscriptionRecord subscription) throws JsonProcessingException {
        // Decrypt the encrypted key from the notification
        final var decryptedKey = Objects.requireNonNull(certificateStore.getEncryptionKey(
                Objects.requireNonNull(notification.encryptedContent.dataKey)));

        // Validate the signature
        if (certificateStore.isDataSignatureValid(
                decryptedKey,
                Objects.requireNonNull(notification.encryptedContent.data),
                Objects.requireNonNull(notification.encryptedContent.dataSignature))) {
            // Decrypt the data using the decrypted key
            final var decryptedData = certificateStore.getDecryptedData(decryptedKey,
                    Objects.requireNonNull(notification.encryptedContent.data));
            // Deserialize the decrypted JSON into a ChatMessage
            final var serializer = new DefaultSerializer(new DefaultLogger());
            final var message = Objects.requireNonNull(
                    serializer.deserializeObject(decryptedData, Message.class));
            // Send the information to subscribed clients
            NewMessageNotification newMessage = MessageAdapter.convert(message);
            Log.infov("***Fireing new message: {0}***", mapper.writeValueAsString(newMessage));
            fireEvent(newMessage);
            //new NewMessageNotification(message);
            

        }
    }

    
    private void fireEvent(NewMessageNotification event) {
        events.fire(event);
    }


}
