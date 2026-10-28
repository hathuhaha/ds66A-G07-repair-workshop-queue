package com.oop.project.model;

import java.time.LocalDate;

public class LegacyWarrantyImport {
    private final int deviceId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String legacyRef;
    private final String note;

    public LegacyWarrantyImport(int deviceId, LocalDate startDate, LocalDate endDate,
            String legacyRef, String note) {
        if (deviceId <= 0) {
            throw new IllegalArgumentException("Device ID must be positive.");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Warranty dates are invalid.");
        }
        if (legacyRef == null || legacyRef.isBlank() || legacyRef.length() > 100) {
            throw new IllegalArgumentException("Legacy reference must contain 1 to 100 characters.");
        }
        this.deviceId = deviceId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.legacyRef = legacyRef;
        this.note = note;
    }

    public int getDeviceId() { return deviceId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getLegacyRef() { return legacyRef; }
    public String getNote() { return note; }
}
