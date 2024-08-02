package com.payslip.payslip.services.util;

import java.util.ArrayList;
import java.util.List;

import com.payslip.events.Payload;
import com.payslip.events.PayslipGenerated;
import com.payslip.events.PayslipRequested;
import com.payslip.payslip.services.jco.PayData;

public class PayslipUtil {
    
    public static List<Payload> makePayload(List<PayData> payDataList) {
        List<Payload> payload = new ArrayList<>();

        for (PayData pd : payDataList) {
            String fileName = createFileName(pd);
            Payload pl = new Payload();
            pl.payslipPdf = pd.getPayslipPdf();
            pl.pdfFileName = fileName;
            payload.add(pl);
        }

        return payload;
    }

    public static String createFileName(PayData pd) {
        String payType;

        if (pd.getOffCycleReason() == null || "".equals(pd.getOffCycleReason().trim())) {
            payType = "Payslip";
        } else {
            payType = sanitize(pd.getOffCycleReason());
        }

        return String.format("%tb_%tY_%s_%s.pdf",
                pd.getPayDate(),
                pd.getPayDate(),
                payType,
                pd.getStaffId());
    }

    public static String sanitize(String text) {
        return text.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static PayslipGenerated makePayslipGeneratedEvent(PayslipRequested request, List<PayData> payDataList) {
        PayslipGenerated payslipGenerated = new PayslipGenerated();
        payslipGenerated.emailFrom = request.emailFrom;
        payslipGenerated.dateSent = request.dateSent;
        payslipGenerated.requestId = request.id;
        payslipGenerated.subject = request.subject;
        payslipGenerated.body = "Please find attached your payslip(s) as requested\n\nSigned:";
        payslipGenerated.payloads = makePayload(payDataList);

        return payslipGenerated;
    }
}
