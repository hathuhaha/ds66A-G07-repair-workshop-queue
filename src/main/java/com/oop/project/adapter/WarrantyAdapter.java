
package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;

public class WarrantyAdapter implements WarrantyResult {
    private final LegacyWarrantyRecord record;

    public WarrantyAdapter(LegacyWarrantyRecord record) {
        this.record = record;
    }

    @Override
    public int getDeviceId() {
        return record.getDeviceId();
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
