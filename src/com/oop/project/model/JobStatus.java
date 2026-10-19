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
}