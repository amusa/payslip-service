/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.payslip.services.jco;

import java.time.LocalDate;

/**
 *
 * @author maliska
 */
public class PayData {

    private String staffId;
    private byte[] payslipPdf;
    private LocalDate payDate;
    private String offCycleReason;

    PayData(String staffId, byte[] payslip, LocalDate pDate, String offCycle) {
        this.staffId = staffId;
        this.payslipPdf = payslip;
        this.payDate = pDate;
        this.offCycleReason = offCycle;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public byte[] getPayslipPdf() {
        return payslipPdf;
    }

    public void setPayslipPdf(byte[] payslipPdf) {
        this.payslipPdf = payslipPdf;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public String getOffCycleReason() {
        return offCycleReason;
    }

    public void setOffCycleReason(String offCycleReason) {
        this.offCycleReason = offCycleReason;
    }

}
