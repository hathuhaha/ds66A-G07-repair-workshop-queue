
package com.oop.project.adapter;

import java.util.Objects;

import com.oop.project.model.WarrantyStatus;

public class WarrantyAdapter implements WarrantyResult {
    private final LegacyWarrantyRecord record;

    public WarrantyAdapter(LegacyWarrantyRecord record) {
        this.record = Objects.requireNonNull(record, "Legacy warranty record is required.");
    }

    @Override
    public int getDeviceId() {
        return record.getDeviceId();
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
