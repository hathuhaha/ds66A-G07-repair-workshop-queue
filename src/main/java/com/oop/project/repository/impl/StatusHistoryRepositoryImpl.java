package com.oop.project.repository.impl;

import com.oop.project.model.StatusHistory;
import com.oop.project.repository.StatusHistoryRepository;
import com.oop.project.repository.mapper.StatusHistoryMapper;

import javax.sql.DataSource;
import java.util.List;

public class StatusHistoryRepositoryImpl extends AbstractJdbcRepository
        implements StatusHistoryRepository {

    private static final String SELECT = "SELECT " + StatusHistoryMapper.columns("h", "") + " FROM status_history h";

    private final StatusHistoryMapper mapper = new StatusHistoryMapper();

    public StatusHistoryRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<StatusHistory> findByJobId(int jobId) {
        return queryList("Lấy lịch sử trạng thái",
                SELECT + " WHERE h.job_id = ? ORDER BY h.changed_at ASC, h.id ASC",
                ps -> ps.setInt(1, jobId), mapper);
    }
}