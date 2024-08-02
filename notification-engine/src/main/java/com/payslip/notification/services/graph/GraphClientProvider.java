package com.payslip.notification.services.graph;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import okhttp3.Request;

@ApplicationScoped
public class GraphClientProvider {

    @ConfigProperty(name = "app.clientId")
    String clientId;

    @ConfigProperty(name = "app.clientSecret")
    String clientSecret;

    @ConfigProperty(name = "app.tenantId")
    String tenantId;

    private GraphServiceClient<Request> graphClient;

    @PostConstruct
    public void construct() {
        Log.infov(
                "***Creating ClientSecretCredential instance:\n\tClientId: {0}\n\tSecret: {1}\n\tTenant Id: {2}***",
                clientId, clientSecret, tenantId);
        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(
                List.of("https://graph.microsoft.com/.default"), clientSecretCredential);

        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredentialAuthProvider)
                .buildClient();

    }

    public GraphServiceClient<Request> getGraphClient() {
        return graphClient;
    }

}