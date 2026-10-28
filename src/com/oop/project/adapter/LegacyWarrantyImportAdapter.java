package com.oop.project.adapter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import com.oop.project.model.LegacyWarrantyImport;
import com.oop.project.model.WarrantyStatus;

public final class LegacyWarrantyImportAdapter {

    public Optional<LegacyWarrantyImport> adapt(
            WarrantyResult source, LocalDate startDate, LocalDate endDate, String legacyRef) {
        Objects.requireNonNull(source, "Legacy warranty result is required.");
        if (source.getWarrantyStatus() == WarrantyStatus.NOT_COVERED) {
            return Optional.empty();
        }
        return Optional.of(new LegacyWarrantyImport(
                source.getSerialNumber(), startDate, endDate, legacyRef, source.getDescription()));
    }
}