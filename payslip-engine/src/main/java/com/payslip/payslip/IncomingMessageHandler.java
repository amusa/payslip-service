package com.payslip.payslip;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.payslip.events.AppEvent;
import com.payslip.events.MonthMarker;
import com.payslip.events.Notification;
import com.payslip.events.PayslipGenerated;
import com.payslip.events.PayslipRequested;
import com.payslip.payslip.services.PayslipService;
import com.payslip.payslip.services.jco.PayData;
import com.payslip.payslip.services.util.PayslipUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class IncomingMessageHandler {

    @Inject
    Event<AppEvent> events;

    @Inject
    private PayslipService payslipService;

    ObjectMapper mapper;

    {
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Incoming("payslip-request")
    @Blocking
    public CompletionStage<Void> receive(Message<PayslipRequested> message) {
        PayslipRequested request = message.getPayload();

        Log.infov(
                "---INCOMING MESSAGE: PayslipRequested Event: \n\tId={0}\n\tSubject={1}\n\tSender={2}\n\tInstant={3}\n\tDateSent={4}\n\tPeriodFrom={5}\n\tPeriodTo={6} ",
                request.id, request.subject, request.emailFrom,
                request.instant.toString(), request.dateSent, request.periodFrom.getDate(),
                request.periodTo.getDate(MonthMarker.END));

        List<PayData> payDataList = null;
        try {
            payDataList = payslipService
                    .getPayslipBytes(request.emailFrom,
                            request.periodFrom.getDate(),
                            request.periodTo.getDate(MonthMarker.END));
        } catch (Exception ex) {
            Log.fatalv("--- firing notification for error message {0} ---", ex.getMessage());

            Notification notice = new Notification();
            notice.id = UUID.randomUUID().toString();
            notice.instant = Instant.now();
            notice.emailFrom = request.emailFrom;
            notice.dateSent = request.dateSent;
            notice.requestId = request.id;
            notice.subject = request.subject;
            notice.message = ex.getMessage();

            events.fire(notice);

            return message.ack();

        }

        Log.infov("---payslipService.getPayslipBytes() executed successfully---\nData:{0}", payDataList);

        PayslipGenerated event = PayslipUtil.makePayslipGeneratedEvent(request, payDataList);
        events.fire(event);

        return message.ack();

    }

}
