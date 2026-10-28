package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;

public class LegacyWarrantyRecord {
    private final int deviceId;
    private final WarrantyStatus warrantyStatus;
    private final String description;

    public LegacyWarrantyRecord(int deviceId, WarrantyStatus warrantyStatus, String description) {
        this.deviceId = deviceId;
        this.warrantyStatus = warrantyStatus;
        this.description = description;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public WarrantyStatus getWarrantyStatus() {
        return warrantyStatus;
    }

    public String getDescription() {
        return description;
    }
}
