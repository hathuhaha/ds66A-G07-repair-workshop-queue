package com.oop.project.model;

import java.time.LocalDateTime;

public class WarrantyCandidate {
    private final int repairId;
    private final String serialNumber;
    private final String deviceName;
    private final String customerName;
    private final LocalDateTime receivedAt;
    private final LocalDateTime completedAt;

    public WarrantyCandidate(int repairId, String serialNumber, String deviceName, String customerName,
            LocalDateTime receivedAt, LocalDateTime completedAt) {
        this.repairId = repairId;
        this.serialNumber = serialNumber;
        this.deviceName = deviceName;
        this.customerName = customerName;
        this.receivedAt = receivedAt;
        this.completedAt = completedAt;
    }

    public int getRepairId() {
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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
