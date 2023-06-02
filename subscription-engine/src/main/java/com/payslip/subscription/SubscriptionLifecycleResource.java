package com.payslip.subscription;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/lifecycle")
public class SubscriptionLifecycleResource {

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
    private SubscriptionManager subscriptionManager;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public CompletableFuture<Response> handleLifecycleValidation(
            @QueryParam("validationToken") String validationToken) {
        Log.infov("***Handling lifecycle notification***\n\tValidation token: {0}", validationToken);

        return CompletableFuture.completedFuture(Response.ok().entity(validationToken).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletableFuture<Response> handleLifecycleNotification(@Nonnull String jsonPayload) throws JsonProcessingException {
        Log.infov("***Handling lifecycle notification***\n\tPayload: {0}", jsonPayload);

        JSONObject jObj = new JSONObject(jsonPayload);
        // String subscriptionId = jObj.getJSONObject("pageInfo").getString("pageName");
        JSONArray jArray = jObj.getJSONArray("value");

        Iterator<Object> iterator = jArray.iterator();

        // while (iterator.hasNext()) {
        JSONObject obj = (JSONObject) iterator.next();
        String eventType = obj.getString("lifecycleEvent");
        String subscriptionId = obj.getString("subscriptionId");

        Log.infov("***Parsing Lifecycle event***\n\teventType: {0}\n\tSubscriptionId:{1}", eventType, subscriptionId);
        // }

        if (eventType.equals("reauthorizationRequired")) {
            Log.infov("***Resubscribing notification: {0}***", subscriptionId);
            subscriptionManager.resubscribe(subscriptionId);
        } else if (eventType.equals("missed")) {
            subscriptionManager.processDeltaMessages(jsonPayload);
        } else {
            Log.infov("***Lifecycle event not \"reauthorizationRequired\"***");
        }

        return CompletableFuture.completedFuture(Response.ok().build());
    }

    
}
