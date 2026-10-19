package com.oop.project.model;

public enum JobStatus {
    RECEIVED("Đã nhận"),
    DIAGNOSING("Đang chẩn đoán"),
    WAITING_FOR_APPROVAL("Đang chờ phê duyệt"),
    REPAIRING("Đang sửa chữa"),
    READY("Sẵn sàng"),
    CLOSED("Đã đóng");

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