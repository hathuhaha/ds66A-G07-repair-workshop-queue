package com.oop.project.repository;

import java.util.Collection;
import java.util.List;

import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;

public interface RepairJobRepository extends GenericRepository<RepairJob, Integer> {

    List<RepairJob> findByStatus(JobStatus status);

    List<RepairJob> findByDeviceId(int deviceId);

    List<RepairJob> findByCustomerId(int customerId);

    /**
     * Hàng đợi ưu tiên: mọi đơn chưa CLOSED, sắp xếp theo hạn chót
     * (created_at + desired_days) rồi created_at rồi id (contract mục 5.1).
     */
    List<RepairJob> findQueue();

    /**
     * Như findQueue() nhưng chỉ lấy các trạng thái được truyền vào.
     * Luôn bỏ qua CLOSED; tập rỗng hoặc null trả về danh sách rỗng.
     */
    List<RepairJob> findQueue(Collection<JobStatus> statuses);

    /**
     * Lưu đơn mới và ghi dòng lịch sử đầu tiên (oldStatus = null, newStatus =
     * RECEIVED)
     * trong MỘT transaction. Dùng cho createJob.
     * Trả về đơn đã có id, createdAt, updatedAt. Đơn mới phải có status null hoặc
     * RECEIVED.
     */
    RepairJob saveWithInitialHistory(RepairJob job, String note);

    /**
     * Cập nhật status, closedAt, updatedAt của đơn rồi ghi dòng lịch sử trong MỘT
     * transaction.
     * Dùng cho updateJobStatusWithHistory. Các field khác của đơn không bị ghi.
     * history.newStatus phải khớp job.status; history.jobId bỏ trống (0) thì tự
     * gán.
     * Đơn CLOSED phải có closedAt, đơn chưa CLOSED phải để closedAt = null.
     */
    void updateWithHistory(RepairJob job, StatusHistory history);
}