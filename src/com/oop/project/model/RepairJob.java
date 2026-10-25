package com.oop.project.model;

import java.time.LocalDateTime;

import com.oop.project.exception.DomainException;

public class RepairJob {
    private int id;
    private Device device;
    private String issueDescription;
    private JobStatus status;
    private int desiredDays;
    private LocalDateTime estimatedReadyAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RepairJob(int id, Device device, String issueDescription, JobStatus status, int desiredDays,
            LocalDateTime estimatedReadyAt, LocalDateTime closedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.device = validateDevice(device);
        this.issueDescription = validateIssueDescription(issueDescription);
        this.status = validateStatus(status);
        this.desiredDays = validateDesiredDays(desiredDays);
        this.estimatedReadyAt = estimatedReadyAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RepairJob(Device device, String issueDescription, int desiredDays) {
        this(0, device, issueDescription, JobStatus.RECEIVED, desiredDays, null, null, null, null);
    }

    private static Device validateDevice(Device device) {
        if (device == null) {
            throw new DomainException("Device must not be empty");
        }
        return device;
    }

    private static String validateIssueDescription(String issueDescription) {
        if (issueDescription == null || issueDescription.isBlank()) {
            throw new DomainException("Issue Description must not be empty");
        }
        return issueDescription.trim();
    }

    private static JobStatus validateStatus(JobStatus status) {
        if (status == null) {
            throw new DomainException("Status must not be empty");
        }
        return status;
    }

    private static int validateDesiredDays(int desiredDays) {
        if (desiredDays <= 0) {
            throw new DomainException("Desired Days must be greater than 0");
        }
        return desiredDays;
    }

    public int getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public JobStatus getStatus() {
        return status;
    }

    public int getDesiredDays() {
        return desiredDays;
    }

    public LocalDateTime getEstimatedReadyAt() {
        return estimatedReadyAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeadline() {
        if (createdAt == null) {
            throw new DomainException("Order has not been created, so no deadline has been set");
        }
        return createdAt.plusDays(desiredDays);
    }

    public boolean isClosed() {
        return status == JobStatus.CLOSED;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(JobStatus status) {
        this.status = validateStatus(status);
    }

    public void setEstimatedReadyAt(LocalDateTime estimatedReadyAt) {
        this.estimatedReadyAt = estimatedReadyAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
