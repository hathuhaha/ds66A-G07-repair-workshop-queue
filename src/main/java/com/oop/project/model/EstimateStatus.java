package com.oop.project.model;

public enum EstimateStatus {
    DRAFT("Draft"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String displayName;

    EstimateStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
