package com.oop.project.model;

public enum WarrantyStatus {
    COVERED("Covered"),
    EXPIRED("Expired"),
    NOT_COVERED("Not Cover");

    private final String displayName;

    WarrantyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
