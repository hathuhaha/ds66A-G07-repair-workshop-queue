package com.oop.project.model;

import java.time.LocalDateTime;

import com.oop.project.exception.DomainException;

public class StatusHistory {
    private int id;
    private int jobId;
    private JobStatus oldStatus;
    private JobStatus newStatus;
    private String note;
    private LocalDateTime changedAt;

    public StatusHistory(int id, int jobId, JobStatus oldStatus, JobStatus newStatus, String note,
            LocalDateTime changedAt) {
        this.id = id;
        this.jobId = validateJobId(jobId);
        this.oldStatus = oldStatus;
        this.newStatus = validateNewStatus(newStatus);
        this.note = normalizeString(note);
        this.changedAt = changedAt;
    }

    public StatusHistory(int jobId, JobStatus oldStatus, JobStatus newStatus, String note) {
        this(0, jobId, oldStatus, newStatus, note, null);
    }

    private static int validateJobId(int jobId) {
        if (jobId <= 0) {
            throw new DomainException("Job ID must be greater than 0");
        }
        return jobId;
    }

    private static JobStatus validateNewStatus(JobStatus newStatus) {
        if (newStatus == null) {
            throw new DomainException("Status must not be empty");
        }
        return newStatus;
    }

    private static String normalizeString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public int getId() {
        return id;
    }

    public int getJobId() {
        return jobId;
    }

    public JobStatus getOldStatus() {
        return oldStatus;
    }

    public JobStatus getNewStatus() {
        return newStatus;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setId(int id) {
        this.id = id;
    }
}
