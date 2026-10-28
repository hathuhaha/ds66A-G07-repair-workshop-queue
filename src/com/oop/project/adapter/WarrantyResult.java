package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;

public interface WarrantyResult {
    String getSerialNumber();

    WarrantyStatus getWarrantyStatus();

    boolean isUnderWarranty();

    String getDescription();
}