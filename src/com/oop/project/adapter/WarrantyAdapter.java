package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;
import java.util.Objects;

public class WarrantyAdapter implements WarrantyResult {
    private final LegacyWarrantyRecord record;

    public WarrantyAdapter(LegacyWarrantyRecord record) {
        this.record = Objects.requireNonNull(record, "Legacy warranty record is required.");
    }

    @Override
    public String getSerialNumber() {
        return record.getSerialNumber();
    }

    @Override
    public WarrantyStatus getWarrantyStatus() {
        return record.getWarrantyStatus();
    }

    @Override
    public boolean isUnderWarranty() {
        return record.getWarrantyStatus() == WarrantyStatus.COVERED;
    }

    @Override
    public String getDescription() {
        return record.getDescription();
    }
}