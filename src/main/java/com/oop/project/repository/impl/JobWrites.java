package com.oop.project.repository.impl;

import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.repository.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Các câu ghi liên quan đến đơn sửa chữa và lịch sử trạng thái, chạy trên một
 * Connection do
 * người gọi cung cấp để nhiều câu lệnh nằm chung một transaction.
 * Dùng chung cho RepairJobRepositoryImpl và EstimateRepositoryImpl.
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
     * Gán 6 tham số của INSERT_JOB (cũng dùng cho UPDATE đầy đủ trong
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
     * Chỉ ghi status, closed_at, updated_at (status và closed_at phải đổi cùng một
     * câu lệnh).
     */
    static void updateJobStatus(Connection c, RepairJob job) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(UPDATE_JOB_STATUS)) {
            ps.setString(1, job.getStatus().name());
            AbstractJdbcRepository.setDateTime(ps, 2, job.getClosedAt());
            ps.setInt(3, job.getId());
            if (ps.executeUpdate() == 0) {
                throw new EntityNotFoundException(
                        "Không tìm thấy đơn sửa chữa có id = " + job.getId());
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
                history.setChangedAt(RowMapper.toLocalDateTime(rs, "changed_at"));
            }
        }
    }

    /**
     * Kiểm tra đầu vào của các hàm "cập nhật đơn kèm lịch sử" trước khi mở
     * transaction, và
     * gán jobId cho history nếu chưa có.
     */
    static void requireJobAndHistory(RepairJob job, StatusHistory history) {
        if (job == null || job.getId() <= 0) {
            throw new IllegalArgumentException("Đơn sửa chữa phải đã được lưu (id > 0)");
        }
        if (job.getStatus() == null) {
            throw new IllegalArgumentException("Đơn sửa chữa phải có trạng thái");
        }
        boolean closed = job.getStatus() == JobStatus.CLOSED;
        if (closed && job.getClosedAt() == null) {
            throw new IllegalArgumentException("Đơn CLOSED phải có closedAt");
        }
        if (!closed && job.getClosedAt() != null) {
            throw new IllegalArgumentException("Đơn chưa CLOSED phải để closedAt = null");
        }
        if (history == null || history.getNewStatus() == null) {
            throw new IllegalArgumentException("Lịch sử phải có trạng thái mới (newStatus)");
        }
        if (history.getNewStatus() != job.getStatus()) {
            throw new IllegalArgumentException(
                    "Trạng thái mới trong lịch sử phải khớp với trạng thái của đơn");
        }
        if (history.getJobId() > 0 && history.getJobId() != job.getId()) {
            throw new IllegalArgumentException("Lịch sử thuộc về một đơn khác");
        }
        history.setJobId(job.getId());
    }
}