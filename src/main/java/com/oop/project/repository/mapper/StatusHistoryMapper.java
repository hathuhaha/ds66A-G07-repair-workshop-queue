package com.oop.project.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.oop.project.model.JobStatus;
import com.oop.project.model.StatusHistory;

public class StatusHistoryMapper implements RowMapper<StatusHistory> {

    private static final String[] COLUMNS = { "id", "job_id", "old_status", "new_status", "note", "changed_at" };

    private final String prefix;

    public StatusHistoryMapper() {
        this("");
    }

    public StatusHistoryMapper(String prefix) {
        this.prefix = prefix;
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public StatusHistory map(ResultSet rs) throws SQLException {
        int id = rs.getInt(prefix + "id");
        int jobId = rs.getInt(prefix + "job_id");
        String oldStatusStr = rs.getString(prefix + "old_status");
        JobStatus oldStatus = oldStatusStr == null ? null : JobStatus.valueOf(oldStatusStr);
        JobStatus newStatus = JobStatus.valueOf(rs.getString(prefix + "new_status"));
        String note = rs.getString(prefix + "note");
        LocalDateTime changedAt = RowMapper.toLocalDateTime(rs, prefix + "changed_at");

        StatusHistory history = new StatusHistory(id, jobId, oldStatus, newStatus, note, changedAt);
        return history;
    }
}