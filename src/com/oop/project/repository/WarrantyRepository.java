package com.oop.project.repository;

import java.time.LocalDate;
import java.util.List;

import com.oop.project.model.LegacyWarrantyImport;
import com.oop.project.model.WarrantyCandidate;
import com.oop.project.model.WarrantyRecord;

public interface WarrantyRepository {
    List<WarrantyRecord> findAll();

    List<WarrantyCandidate> findCandidates();

    WarrantyRecord createForRepair(int repairId, LocalDate startDate, LocalDate endDate);

    int importLegacy(List<LegacyWarrantyImport> records);
}
