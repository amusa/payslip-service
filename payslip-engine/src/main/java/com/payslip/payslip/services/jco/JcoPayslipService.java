/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.payslip.services.jco;

import com.payslip.payslip.services.PayslipService;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class JcoPayslipService implements PayslipService {

    @ConfigProperty(name = "jco.rfc-dest")
    private String sapRfcDestination;

    @ConfigProperty(name = "jco.ashost")
    private String jcoHost;

    @ConfigProperty(name = "jco.sysnr")
    private String jcoSysNr;

    @ConfigProperty(name = "jco.client")
    private String jcoClient;

    @ConfigProperty(name = "jco.user")
    private String jcoUser;

    @ConfigProperty(name = "jco.passwd")
    private String jcoPassword;

    @ConfigProperty(name = "jco.lang")
    private String jcoLang;

    @ConfigProperty(name = "jco.pool-capacity")
    private String jcoPoolCapacity;

    @ConfigProperty(name = "jco.peak-limit")
    private String jcoPeakLimit;

    @PostConstruct
    public void init() {
        Properties connectProperties = new Properties();
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, jcoHost);
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, jcoSysNr);
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, jcoClient);
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, jcoUser);
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, jcoPassword);
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, jcoLang);
        connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, jcoPoolCapacity);
        connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, jcoPeakLimit);
        createDestinationDataFile(sapRfcDestination, connectProperties);
    }

    private void createDestinationDataFile(String destinationName, Properties connectProperties) {
        File destCfg = new File(destinationName + ".jcoDestination");

        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "SAP jco destination config");
            fos.close();
        } catch (IOException e) {
            Log.warnv("Unable to create the destination files");
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }

    @Override
    //@CircuitBreaker(successThreshold = 2, requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Retry(retryOn = { JCoException.class}, maxRetries = 2, maxDuration = 10000)
    public List<PayData> getPayslipBytes(String email, LocalDate dateFrom, LocalDate dateTo) throws JCoException {
        Log.infov("--- getPayslipBytes called with parameters: Email={0}, dateFrom={1}, dateTo={2} ---",
                new Object[] { email, dateFrom, dateTo });
        List<PayData> payDataList = new ArrayList<>();

        JCoDestination destination;
        Log.info("---CHECKPOINT #0---");
        destination = JCoDestinationManager.getDestination(sapRfcDestination);

        Log.info("---CHECKPOINT #1---");

        Log.tracev("--- jco destination initialized ---");
        JCoFunction bapiGetIByEmailFunction = destination.getRepository().getFunction("ZUSER_GET_BY_EMAIL");
        JCoFunction bapiPayListFunction = destination.getRepository().getFunction("ZBAPI_GET_PAYROLL_RESULT_LIST");
        Log.tracev("--- jco bapiPayListFunction initialized ---");
        JCoFunction bapiPayslipPdfFunction = destination.getRepository().getFunction("ZBAPI_GET_PAYSLIP_PDF");
        Log.tracev("--- jco bapiPayslipPdfFunction initialized ---");

        Log.info("---CHECKPOINT #2---");

        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (bapiGetIByEmailFunction == null) {
            Log.tracev("ZUSER_GET_BY_EMAIL not found in SAP.");
            throw new RuntimeException("ZUSER_GET_BY_EMAIL not found in SAP.");
        }
        Log.info("---CHECKPOINT #3---");

        if (bapiPayListFunction == null) {
            Log.tracev("ZBAPI_GET_PAYROLL_RESULT_LIST not found in SAP.");
            throw new RuntimeException("ZBAPI_GET_PAYROLL_RESULT_LIST not found in SAP.");
        }

        Log.info("---CHECKPOINT #4---");

        if (bapiPayslipPdfFunction == null) {
            Log.tracev("ZBAPI_GET_PAYSLIP_PDF not found in SAP.");
            throw new RuntimeException("ZBAPI_GET_PAYSLIP_PDF not found in SAP.");
        }

        Log.info("---CHECKPOINT #5---");

        Log.tracev("--- Begining session ---");

        JCoContext.begin(destination);
        try {
            bapiGetIByEmailFunction.getImportParameterList().setValue("EMAIL", email.toUpperCase());
            bapiGetIByEmailFunction.execute(destination);
            Log.tracev("--- Executed 'ZUSER_GET_BY_EMAIL' successfully ---");
            Log.info("---CHECKPOINT #6---");
            JCoStructure userIdReturnStructure = bapiGetIByEmailFunction.getExportParameterList()
                    .getStructure("RESULT");
            Log.info("---CHECKPOINT #7---");
            String staffId = userIdReturnStructure.getString("PERNR");
            String retEmail = userIdReturnStructure.getString("USRID_LONG");
            Log.tracev("--- StaffId and Email returned from 'ZUSER_GET_BY_EMAIL':{0}, {1} ---",
                    new Object[] { staffId, retEmail });

            staffId = staffId.replaceFirst("^0+", "");

            if (null == staffId || staffId.isEmpty()) {
                throw new RuntimeException(String.format(
                        "Your email: %s does not match the email in your SAP profile. \n\nPlease contact HCM.", email));
            }
            Log.info("---CHECKPOINT #8---");
            bapiPayListFunction.getImportParameterList().setValue("EMPLOYEENUMBER", staffId);
            bapiPayListFunction.getImportParameterList().setValue("FROMDATE", formatter.format(dateFrom));
            bapiPayListFunction.getImportParameterList().setValue("TODATE", formatter.format(dateTo));
            Log.info("---CHECKPOINT #9---");
            bapiPayListFunction.execute(destination);
            Log.tracev("--- Executed 'BAPI_GET_PAYROLL_RESULT_LIST' successfully ---");
            Log.info("---CHECKPOINT #10---");
            JCoStructure payrollReturnStructure = bapiPayListFunction.getExportParameterList().getStructure("RETURN");
            if (payrollReturnStructure.getString("TYPE").equals("E")) {
                throw new RuntimeException(payrollReturnStructure.getString("MESSAGE"));
            }

            JCoTable resultTable = bapiPayListFunction.getTableParameterList().getTable("RESULTS");
            String sequenceNumber;
            LocalDate payDate;
            String offCycleReason;
            Log.info("---CHECKPOINT #11---");
            for (int i = 0; i < resultTable.getNumRows(); i++, resultTable.nextRow()) {
                sequenceNumber = resultTable.getString("SEQUENCENUMBER");
                payDate = LocalDate.ofInstant(resultTable.getDate("PAYDATE").toInstant(), ZoneId.systemDefault());
                offCycleReason = resultTable.getString("OCREASON_TEXT");

                bapiPayslipPdfFunction.getImportParameterList().setValue("EMPLOYEENUMBER", staffId);
                bapiPayslipPdfFunction.getImportParameterList().setValue("SEQUENCENUMBER", sequenceNumber);
                bapiPayslipPdfFunction.getImportParameterList().setValue("PAYSLIPVARIANT", "NNPC-PS");

                try {
                    bapiPayslipPdfFunction.execute(destination);
                    Log.tracev("--- Executed 'ZBAPI_GET_PAYSLIP_PDF' successfully ---");

                    JCoStructure pdfReturnStructure = bapiPayslipPdfFunction.getExportParameterList()
                            .getStructure("RETURN");
                    if (pdfReturnStructure.getString("TYPE").equals("E")) {
                        throw new RuntimeException(pdfReturnStructure.getString("MESSAGE"));
                    }

                    byte[] payslip = bapiPayslipPdfFunction.getExportParameterList().getByteArray("PAYSLIP");

                    Log.tracev("--- Payslip Pdf returned  ---");

                    PayData payData = new PayData(staffId, payslip, payDate, offCycleReason);
                    payDataList.add(payData);

                } catch (AbapException ex) {
                    Log.errorv("Error executing ZBAPI_GET_PAYSLIP_PDF.");
                    throw new RuntimeException("Error executing ZBAPI_GET_PAYSLIP_PDF.");
                }
            }

        } catch (AbapException ex) {
            Log.errorv("Error executing ZBAPI_GET_PAYROLL_RESULT_LIST.");
            throw new RuntimeException("Error executing ZBAPI_GET_PAYROLL_RESULT_LIST.");
        }

        Log.infov("--- Returning pay data list ---");

        return payDataList;
    }

}
