package com.oop.project.model;

public enum JobStatus {
    RECEIVED("Received"),
    DIAGNOSING("Diagnosing"),
    WAITING_FOR_APPROVAL("Waiting for Approval"),
    REPAIRING("Repairing"),
    READY("Ready"),
    CLOSED("Closed");

    private final String displayName;

    JobStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(JobStatus next) {
        return switch (this) {
            case RECEIVED -> next == DIAGNOSING;
            case DIAGNOSING -> next == WAITING_FOR_APPROVAL;
            case WAITING_FOR_APPROVAL -> next == REPAIRING || next == CLOSED;
            case REPAIRING -> next == READY;
            case READY -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}