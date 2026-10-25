package com.oop.project.repository;

import java.util.Collection;
import java.util.List;

import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;

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
}