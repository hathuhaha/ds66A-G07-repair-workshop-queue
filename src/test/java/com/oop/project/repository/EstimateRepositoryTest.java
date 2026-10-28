package com.oop.project.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;
import com.oop.project.repository.impl.EstimateRepositoryImpl;
import com.oop.project.support.TestDatabase;

class EstimateRepositoryTest {

    private EstimateRepository repository;

    @BeforeEach
    void setUp() {
        TestDatabase.reset();
        repository = new EstimateRepositoryImpl(TestDatabase.dataSource());
    }

    @Test
    void findByIdLoadsPartsAndLaborServices() {
        Estimate estimate = repository.findById(2).orElseThrow(); // 2 parts, 1 labor in the seed
        assertEquals(2, estimate.getParts().size());
        assertEquals(1, estimate.getLaborServices().size());
    }

    @Test
    void secondEstimateOfTheSameJobGetsVersion2() {
        Estimate saved = repository.save(new Estimate(2, "Revised quote", List.of(), List.of()));
        assertTrue(saved.getId() > 0);
        assertEquals(2, saved.getVersion());
        assertEquals(saved.getId(), repository.findLatestByJobId(2).orElseThrow().getId());
    }

    @Test
    void updateStatusChangesTheEstimate() {
        repository.updateStatus(2, EstimateStatus.REJECTED);
        assertEquals(EstimateStatus.REJECTED, repository.findById(2).orElseThrow().getStatus());
    }
}