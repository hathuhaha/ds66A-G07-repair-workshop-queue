package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;

public interface WarrantyResult {
    int getDeviceId();

    WarrantyStatus getWarrantyStatus();

    boolean isUnderWarranty();

    String getDescription();
}
