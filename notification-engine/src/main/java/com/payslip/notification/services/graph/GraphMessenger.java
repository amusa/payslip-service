/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.notification.services.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;

import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import com.payslip.events.Notification;
import com.payslip.events.Payload;
import com.payslip.events.PayslipGenerated;
import com.payslip.notification.services.Messenger;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.Request;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class GraphMessenger implements Messenger {

        @ConfigProperty(name = "app.userId")
        String userId;

        @Inject
        GraphClientProvider graphClientProvider;

        ObjectMapper mapper;

        {
                mapper = JsonMapper.builder()
                                .findAndAddModules()
                                .build();
        }

        @Override
        // @Retry(maxRetries = 5, maxDuration = 100000, retryOn = { Exception.class })
        // @CircuitBreaker(requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
        public void send(PayslipGenerated event, boolean retry) {

                GraphServiceClient<Request> graphClient = graphClientProvider.getGraphClient();

                // Create a new message
                final Message message = new Message();
                message.subject = String.format("RE:%s", event.subject);
                message.body = new ItemBody();
                message.body.content = event.body;
                message.body.contentType = BodyType.TEXT;

                final Recipient toRecipient = new Recipient();
                toRecipient.emailAddress = new EmailAddress();
                toRecipient.emailAddress.address = event.emailFrom;
                message.toRecipients = List.of(toRecipient);

                Log.infov("--- Attaching payslip ---");
                LinkedList<Attachment> attachmentsList = new LinkedList<Attachment>();

                for (Payload pl : event.payloads) {
                        FileAttachment fileAttachment = new FileAttachment();
                        fileAttachment.name = pl.pdfFileName;
                        fileAttachment.contentBytes = pl.payslipPdf;
                        fileAttachment.oDataType = "#microsoft.graph.fileAttachment";
                        attachmentsList.add(fileAttachment);
                }

                AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
                attachmentCollectionResponse.value = attachmentsList;
                AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(
                                attachmentCollectionResponse,
                                null);
                // add to your message
                message.attachments = attachmentCollectionPage;

                Log.infov("--- Sending email ---");
                // Send the message
                graphClient.users(userId)
                                .sendMail(UserSendMailParameterSet.newBuilder()
                                                .withMessage(message)
                                                .build())
                                .buildRequest()
                                .postAsync();

        }

        @Override
        // @Retry(maxRetries = 5, maxDuration = 100000, retryOn = { Exception.class })
        // @CircuitBreaker(requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
        public void send(Notification event, boolean retry) {
                GraphServiceClient<Request> graphClient = graphClientProvider.getGraphClient();
                // Create a new message
                final Message message = new Message();
                message.subject = String.format("RE:%s", event.subject);
                message.body = new ItemBody();
                message.body.content = event.message;
                message.body.contentType = BodyType.TEXT;

                final Recipient toRecipient = new Recipient();
                toRecipient.emailAddress = new EmailAddress();
                toRecipient.emailAddress.address = event.emailFrom;
                message.toRecipients = List.of(toRecipient);

                try {
                        Log.infov("--- Sending email ---\n\t {0}", mapper.writeValueAsString(message));
                } catch (JsonProcessingException e) {
                        e.printStackTrace();
                }
                graphClient.users(userId)
                                .sendMail(UserSendMailParameterSet.newBuilder()
                                                .withMessage(message)
                                                .build())
                                .buildRequest()
                                .postAsync();

        }

}
