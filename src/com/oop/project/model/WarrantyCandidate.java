package com.oop.project.model;

import java.time.LocalDateTime;

public class WarrantyCandidate {
    private final int repairId;
    private final int deviceId;
    private final String deviceName;
    private final String customerName;
    private final LocalDateTime receivedAt;
    private final LocalDateTime completedAt;

    public WarrantyCandidate(int repairId, int deviceId, String deviceName, String customerName,
            LocalDateTime receivedAt, LocalDateTime completedAt) {
        this.repairId = repairId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.customerName = customerName;
        this.receivedAt = receivedAt;
        this.completedAt = completedAt;
    }

    public int getRepairId() { return repairId; }
    public int getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
