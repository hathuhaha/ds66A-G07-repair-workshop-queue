package com.oop.project.repository;

import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;

import java.util.List;
import java.util.Optional;

public interface EstimateRepository {

    /**
     * Saves a new estimate (id must be 0) with its parts and laborServices in one
     * transaction. The version is always assigned by the repository (highest
     * version of the job + 1); any version already set on the estimate is
     * overwritten.
     * Returns the estimate with id, version, createdAt, updatedAt and the id of
     * each line set. Throws EntityNotFoundException if the job does not exist.
     */
    Estimate save(Estimate estimate);

    /** Nạp kèm parts và laborServices. */
    Optional<Estimate> findById(int id);

    /** Các báo giá của một đơn, sắp xếp theo version tăng dần, nạp kèm các dòng. */
    List<Estimate> findByJobId(int jobId);

    /** Báo giá có version lớn nhất của đơn, nạp kèm các dòng. */
    Optional<Estimate> findLatestByJobId(int jobId);

    void updateStatus(int estimateId, EstimateStatus status);

    /**
     * Đổi trạng thái báo giá, cập nhật đơn (status, closedAt, updatedAt) và ghi
     * dòng lịch sử
     * trong MỘT transaction. Dùng cho:
     * - approveEstimate: APPROVED, đơn chuyển sang REPAIRING.
     * - rejectEstimate: REJECTED, đơn chuyển sang CLOSED (kèm closedAt).
     * Báo giá phải thuộc đơn được truyền vào, nếu không ném
     * EntityNotFoundException.
     */
    void updateEstimateStatusAndJob(int estimateId, EstimateStatus status,
            RepairJob job, StatusHistory history);
}