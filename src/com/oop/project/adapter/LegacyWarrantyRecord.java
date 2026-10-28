package com.oop.project.adapter;

import java.util.Objects;

import com.oop.project.model.WarrantyStatus;

public class LegacyWarrantyRecord {
    private final int deviceId;
    private final WarrantyStatus warrantyStatus;
    private final String description;

    public LegacyWarrantyRecord(int deviceId, WarrantyStatus warrantyStatus, String description) {
        if (deviceId <= 0) {
            throw new IllegalArgumentException("Device ID must be positive.");
        }
        this.deviceId = deviceId;
        this.warrantyStatus = Objects.requireNonNull(warrantyStatus, "Warranty status is required.");
        this.description = description == null ? "" : description.trim();
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
