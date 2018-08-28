/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.fulfilment.infrastructure.jco;

import com.payslip.fulfilment.PayslipService;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class JCoPayslipService implements PayslipService {

    private static final Logger logger = Logger.getLogger(JCoPayslipService.class.getName());

    @Inject
    @ConfigProperty(name = "SAP_RFC_DESTINATION")
    private String sapRfcDestination;

    @Inject
    @ConfigProperty(name = "JCO_ASHOST")
    private String jcoHost;

    @Inject
    @ConfigProperty(name = "JCO_SYSNR")
    private String jcoSysNr;

    @Inject
    @ConfigProperty(name = "JCO_CLIENT")
    private String jcoClient;

    @Inject
    @ConfigProperty(name = "JCO_USER")
    private String jcoUser;

    @Inject
    @ConfigProperty(name = "JCO_PASSWD")
    private String jcoPassword;

    @Inject
    @ConfigProperty(name = "JCO_LANG")
    private String jcoLang;

    @Inject
    @ConfigProperty(name = "JCO_POOL_CAPACITY")
    private String jcoPoolCapacity;

    @Inject
    @ConfigProperty(name = "JCO_PEAK_LIMIT")
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
       // logger.log(Level.INFO, "--- Jco service initialized ---\n{0}", connectProperties);
    }

    private void createDestinationDataFile(String destinationName, Properties connectProperties) {
        File destCfg = new File(destinationName + ".jcoDestination");

        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "SAP jco destination config");
            fos.close();
        } catch (IOException e) {
            logger.warning("Unable to create the destination files");
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }

    @Override
    // @CircuitBreaker(successThreshold = 2, requestVolumeThreshold = 4, failureRatio = 0.75, delay = 50000)
    @Retry(retryOn = {JCoException.class}, maxRetries = 7, maxDuration = 100000)
    public List<PayData> getPayslipBytes(String email, Date dateFrom, Date dateTo) throws JCoException {
        logger.log(Level.INFO, "--- getPayslipBytes called with parameters: Email={0}, dateFrom={1}, dateTo={2} ---",
                new Object[]{email, dateFrom, dateTo});
        List<PayData> payDataList = new ArrayList<>();

        JCoDestination destination;

        destination = JCoDestinationManager.getDestination(sapRfcDestination);

        logger.log(Level.INFO, "--- jco destination initialized ---");
        JCoFunction bapiGetIByEmailFunction = destination.getRepository().getFunction("ZUSER_GET_BY_EMAIL");
        JCoFunction bapiPayListFunction = destination.getRepository().getFunction("ZBAPI_GET_PAYROLL_RESULT_LIST");
        logger.log(Level.INFO, "--- jco bapiPayListFunction initialized ---");
        JCoFunction bapiPayslipPdfFunction = destination.getRepository().getFunction("ZBAPI_GET_PAYSLIP_PDF");
        logger.log(Level.INFO, "--- jco bapiPayslipPdfFunction initialized ---");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (bapiGetIByEmailFunction == null) {
            logger.log(Level.INFO, "ZUSER_GET_BY_EMAIL not found in SAP.");
            throw new RuntimeException("ZUSER_GET_BY_EMAIL not found in SAP.");
        }

        if (bapiPayListFunction == null) {
            logger.log(Level.INFO, "ZBAPI_GET_PAYROLL_RESULT_LIST not found in SAP.");
            throw new RuntimeException("ZBAPI_GET_PAYROLL_RESULT_LIST not found in SAP.");
        }

        if (bapiPayslipPdfFunction == null) {
            logger.log(Level.INFO, "ZBAPI_GET_PAYSLIP_PDF not found in SAP.");
            throw new RuntimeException("ZBAPI_GET_PAYSLIP_PDF not found in SAP.");
        }

        logger.log(Level.INFO, "--- Begining session ---");

        JCoContext.begin(destination);
        try {
            bapiGetIByEmailFunction.getImportParameterList().setValue("EMAIL", email.toUpperCase());
            logger.log(Level.INFO, "--- Executing 'ZUSER_GET_BY_EMAIL' ---");
            bapiGetIByEmailFunction.execute(destination);
            logger.log(Level.INFO, "--- Executed 'ZUSER_GET_BY_EMAIL' successfully ---");

            JCoStructure userIdReturnStructure = bapiGetIByEmailFunction.getExportParameterList().getStructure("RESULT");
                     String staffId = userIdReturnStructure.getString("PERNR");
            String retEmail = userIdReturnStructure.getString("USRID_LONG");
            logger.log(Level.INFO, "--- StaffId and Email returned from 'ZUSER_GET_BY_EMAIL':{0}, {1} ---", new Object[]{staffId, retEmail});

            staffId = staffId.replaceFirst("^0+", "");
           

            if (null == staffId || staffId.isEmpty()) {
                throw new RuntimeException("User ID could not be derived using email provided");
            }
            
            bapiPayListFunction.getImportParameterList().setValue("EMPLOYEENUMBER", staffId);
            bapiPayListFunction.getImportParameterList().setValue("FROMDATE", sdf.format(dateFrom));
            bapiPayListFunction.getImportParameterList().setValue("TODATE", sdf.format(dateTo));

            logger.log(Level.INFO, "--- Executing 'BAPI_GET_PAYROLL_RESULT_LIST' ---");
            bapiPayListFunction.execute(destination);
            logger.log(Level.INFO, "--- Executed 'BAPI_GET_PAYROLL_RESULT_LIST' successfully ---");

            JCoStructure payrollReturnStructure = bapiPayListFunction.getExportParameterList().getStructure("RETURN");
            if (payrollReturnStructure.getString("TYPE").equals("E")) {
                throw new RuntimeException(payrollReturnStructure.getString("MESSAGE"));
            }

            JCoTable resultTable = bapiPayListFunction.getTableParameterList().getTable("RESULTS");
            String sequenceNumber;
            Date payDate;
            String offCycleReason;

            for (int i = 0; i < resultTable.getNumRows(); i++, resultTable.nextRow()) {
                sequenceNumber = resultTable.getString("SEQUENCENUMBER");
                payDate = resultTable.getDate("PAYDATE");
                offCycleReason = resultTable.getString("OCREASON_TEXT");

                bapiPayslipPdfFunction.getImportParameterList().setValue("EMPLOYEENUMBER", staffId);
                bapiPayslipPdfFunction.getImportParameterList().setValue("SEQUENCENUMBER", sequenceNumber);
                bapiPayslipPdfFunction.getImportParameterList().setValue("PAYSLIPVARIANT", "NNPC-PS");

                try {
                    logger.log(Level.INFO, "--- Executing 'ZBAPI_GET_PAYSLIP_PDF' ---");
                    bapiPayslipPdfFunction.execute(destination);
                    logger.log(Level.INFO, "--- Executed 'ZBAPI_GET_PAYSLIP_PDF' successfully ---");

                    JCoStructure pdfReturnStructure = bapiPayslipPdfFunction.getExportParameterList().getStructure("RETURN");
                    if (pdfReturnStructure.getString("TYPE").equals("E")) {
                        throw new RuntimeException(pdfReturnStructure.getString("MESSAGE"));
                    }

                    byte[] payslip = bapiPayslipPdfFunction.getExportParameterList().getByteArray("PAYSLIP");

                    logger.log(Level.INFO, "--- Payslip Pdf returned  ---");

                    PayData payData = new PayData(staffId, payslip, payDate, offCycleReason);
                    payDataList.add(payData);

                } catch (AbapException ex) {
                    logger.log(Level.SEVERE, "Error executing ZBAPI_GET_PAYSLIP_PDF.");
                    throw new RuntimeException("Error executing ZBAPI_GET_PAYSLIP_PDF.");
                }
            }

        } catch (AbapException ex) {
            logger.log(Level.SEVERE, "Error executing ZBAPI_GET_PAYROLL_RESULT_LIST.");
            throw new RuntimeException("Error executing ZBAPI_GET_PAYROLL_RESULT_LIST.");
        }
//        } catch (JCoException ex) {
//            logger.log(Level.SEVERE, ex.getMessage());
//            throw ex;
//        } finally {
//            JCoContext.end(destination);
//        }

        logger.log(Level.INFO, "--- Returning pay data list ---");

        return payDataList;
    }

}
