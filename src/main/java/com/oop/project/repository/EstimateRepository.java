package com.oop.project.repository;

import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;

import java.util.List;
import java.util.Optional;

public interface EstimateRepository {

    /**
     * Lưu báo giá mới kèm các dòng parts và laborServices trong một transaction.
     * Nếu estimate.getVersion() <= 0 thì version được tự gán (lớn nhất của đơn +
     * 1).
     * Trả về estimate đã có id, version và id của từng dòng.
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