package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;
import java.util.Objects;

public class LegacyWarrantyRecord {
    private final String serialNumber; // Đổi từ int deviceId sang String serialNumber
    private final WarrantyStatus warrantyStatus;
    private final String description;

    public LegacyWarrantyRecord(String serialNumber, WarrantyStatus warrantyStatus, String description) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number must not be empty.");
        }
        this.serialNumber = serialNumber.trim();
        this.warrantyStatus = Objects.requireNonNull(warrantyStatus, "Warranty status is required.");
        this.description = description == null ? "" : description.trim();
    }

    public String getSerialNumber() { // Đổi getter
        return serialNumber;
    }

    public WarrantyStatus getWarrantyStatus() {
        return warrantyStatus;
    }

    public String getDescription() {
        return description;
    }
}