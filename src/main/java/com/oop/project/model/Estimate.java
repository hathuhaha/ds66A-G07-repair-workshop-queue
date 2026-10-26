package com.oop.project.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oop.project.exception.DomainException;

public class Estimate {
    private int id;
    private int jobId;
    private int version;
    private EstimateStatus status;
    private String note;
    private List<Part> parts;
    private List<LaborService> laborServices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Estimate(int id, int jobId, int version, EstimateStatus status, String note, List<Part> parts,
            List<LaborService> laborServices, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jobId = validateJobId(jobId);
        this.version = validateVersion(version);
        this.status = validateStatus(status);
        this.note = normalizeString(note);
        this.parts = normalizeList(parts);
        this.laborServices = normalizeList(laborServices);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Estimate(int jobId, String note, List<Part> parts, List<LaborService> laborServices) {
        this(0, jobId, 1, EstimateStatus.DRAFT, note, parts, laborServices, null, null);
    }

    private static int validateJobId(int jobId) {
        if (jobId <= 0) {
            throw new DomainException("Job ID must be greater than 0");
        }
        return jobId;
    }

    private static int validateVersion(int version) {
        if (version < 1) {
            throw new DomainException("Version must be at least 1");
        }
        return version;
    }

    private static EstimateStatus validateStatus(EstimateStatus status) {
        if (status == null) {
            throw new DomainException("Status must not be empty");
        }
        return status;
    }

    private static String normalizeString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static <T> List<T> normalizeList(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list);
    }

    public int getId() {
        return id;
    }

    public int getJobId() {
        return jobId;
    }

    public int getVersion() {
        return version;
    }

    public EstimateStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public List<Part> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public List<LaborService> getLaborServices() {
        return Collections.unmodifiableList(laborServices);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getTotalCost() {
        BigDecimal total = BigDecimal.ZERO;
        for (Part part : parts) {
            total = total.add(part.getCost());
        }
        for (LaborService labor : laborServices) {
            total = total.add(labor.getCost());
        }
        return total;
    }

    public void addPart(Part part) {
        if (part == null) {
            throw new DomainException("Part must not be empty");
        }
        parts.add(part);
    }

    public void addLaborService(LaborService laborService) {
        if (laborService == null) {
            throw new DomainException("Labor Service must not be empty");
        }
        laborServices.add(laborService);
    }

    public void setId(int id) {
        this.id = id;
        for (Part part : parts) {
            part.setEstimateId(id);
        }
        for (LaborService labor : laborServices) {
            labor.setEstimateId(id);
        }
    }

    public void setVersion(int version) {
        this.version = validateVersion(version);
    }

    public void setStatus(EstimateStatus status) {
        this.status = validateStatus(status);
    }

    public boolean isDraft() {
        return status == EstimateStatus.DRAFT;
    }

    public boolean isApproved() {
        return status == EstimateStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == EstimateStatus.REJECTED;
    }
}
