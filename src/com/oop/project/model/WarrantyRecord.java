package com.oop.project.model;

import java.time.LocalDate;

public class WarrantyRecord {
    private final int warrantyId;
    private final Integer repairId;
    private final String serialNumber;
    private final String deviceName;
    private final String customerName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String source;
    private final String status;

    public WarrantyRecord(int warrantyId, Integer repairId, String serialNumber, String deviceName, String customerName,
            LocalDate startDate, LocalDate endDate, String source, String status) {
        this.warrantyId = warrantyId;
        this.serialNumber = serialNumber;
        this.repairId = repairId;
        this.deviceName = deviceName;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.source = source;
        this.status = status;
    }

    public int getWarrantyId() {
        return warrantyId;
    }

    public Integer getRepairId() {
        return repairId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getSource() {
        return source;
    }

    public String getStatus() {
        return status;
    }
}
