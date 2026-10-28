package com.oop.project.model;

import java.time.LocalDate;

public class LegacyWarrantyImport {
    private final String serialNumber;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String legacyRef;
    private final String note;

    public LegacyWarrantyImport(String serialNumber, LocalDate startDate, LocalDate endDate,
            String legacyRef, String note) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number must not be empty.");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Warranty dates are invalid.");
        }
        if (legacyRef == null || legacyRef.isBlank() || legacyRef.length() > 100) {
            throw new IllegalArgumentException("Legacy reference must contain 1 to 100 characters.");
        }
        this.serialNumber = serialNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.legacyRef = legacyRef;
        this.note = note;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getLegacyRef() {
        return legacyRef;
    }

    public String getNote() {
        return note;
    }
}
