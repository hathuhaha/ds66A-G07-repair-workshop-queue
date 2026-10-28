package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.repository.mapper.RowMapper;

/**
 * Write statements for repair jobs and status history. They run on a Connection
 * supplied by the caller so that several statements share one transaction.
 * Shared by RepairJobRepositoryImpl and EstimateRepositoryImpl.
 */
final class JobWrites {

    static final String INSERT_JOB = "INSERT INTO repair_jobs (device_id, issue_description, status, desired_days, "
            + "estimated_ready_at, closed_at) VALUES (?, ?, ?, ?, ?, ?) "
            + "RETURNING id, created_at, updated_at";

    private static final String UPDATE_JOB_STATUS = "UPDATE repair_jobs SET status = ?, closed_at = ?, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    private static final String INSERT_HISTORY = "INSERT INTO status_history (job_id, old_status, new_status, note) "
            + "VALUES (?, ?, ?, ?) RETURNING id, changed_at";

    private JobWrites() {
    }

    /**
     * Binds the 6 parameters of INSERT_JOB (also used for the full UPDATE in
     * RepairJobRepositoryImpl).
     */
    static void bind(PreparedStatement ps, RepairJob job) throws SQLException {
        ps.setInt(1, job.getDevice().getId());
        ps.setString(2, job.getIssueDescription());
        ps.setString(3, job.getStatus().name());
        ps.setInt(4, job.getDesiredDays());
        AbstractJdbcRepository.setDateTime(ps, 5, job.getEstimatedReadyAt());
        AbstractJdbcRepository.setDateTime(ps, 6, job.getClosedAt());
    }

    static void insertJob(Connection c, RepairJob job) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(INSERT_JOB)) {
            bind(ps, job);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                job.setId(rs.getInt("id"));
                job.setCreatedAt(RowMapper.toLocalDateTime(rs, "created_at"));
                job.setUpdatedAt(RowMapper.toLocalDateTime(rs, "updated_at"));
            }
        }
    }

    /**
     * Writes only status, closed_at and updated_at (status and closed_at must
     * change in the same statement).
     */
    static void updateJobStatus(Connection c, RepairJob job) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(UPDATE_JOB_STATUS)) {
            ps.setString(1, job.getStatus().name());
            AbstractJdbcRepository.setDateTime(ps, 2, job.getClosedAt());
            ps.setInt(3, job.getId());
            if (ps.executeUpdate() == 0) {
                throw new EntityNotFoundException(
                        "Repair job not found with id = " + job.getId());
            }
        }
    }

    static void insertHistory(Connection c, StatusHistory history) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(INSERT_HISTORY)) {
            ps.setInt(1, history.getJobId());
            ps.setString(2, history.getOldStatus() == null ? null : history.getOldStatus().name());
            ps.setString(3, history.getNewStatus().name());
            ps.setString(4, history.getNote());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                history.setId(rs.getInt("id"));
            }
        }
    }

    /**
     * Validates the input of the "update job with history" methods before a
     * transaction is opened. StatusHistory is immutable, so jobId must already be
     * set by its constructor.
     */
    static void requireJobAndHistory(RepairJob job, StatusHistory history) {
        if (job == null || job.getId() <= 0) {
            throw new IllegalArgumentException("Repair job must be saved (id > 0)");
        }
        if (job.getStatus() == null) {
            throw new IllegalArgumentException("Repair job must have a status");
        }
        boolean closed = job.getStatus() == JobStatus.CLOSED;
        if (closed && job.getClosedAt() == null) {
            throw new IllegalArgumentException("A CLOSED job must have closedAt");
        }
        if (!closed && job.getClosedAt() != null) {
            throw new IllegalArgumentException("A job that is not CLOSED must have closedAt = null");
        }
        if (history == null || history.getNewStatus() == null) {
            throw new IllegalArgumentException("History must have a new status (newStatus)");
        }
        if (history.getNewStatus() != job.getStatus()) {
            throw new IllegalArgumentException(
                    "The new status in the history must match the status of the job");
        }
        if (history.getJobId() <= 0) {
            throw new IllegalArgumentException("History must belong to a valid job (jobId > 0)");
        }
        if (history.getJobId() != job.getId()) {
            throw new IllegalArgumentException("History belongs to a different job");
        }
    }
}