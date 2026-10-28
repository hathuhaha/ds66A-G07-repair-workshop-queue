package com.oop.project.repository;

import com.oop.project.model.StatusHistory;

import java.util.List;

public interface StatusHistoryRepository {

    /** Toàn bộ lịch sử, sắp xếp theo thời gian tăng dần và id. */
    List<StatusHistory> findAll();

    /** Lịch sử trạng thái của một đơn, sắp xếp theo changedAt tăng dần. */
    List<StatusHistory> findByJobId(int jobId);
}