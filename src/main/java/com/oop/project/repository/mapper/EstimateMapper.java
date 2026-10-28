package com.oop.project.repository.mapper;

import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EstimateMapper implements RowMapper<Estimate> {

    private static final String[] COLUMNS = { "id", "job_id", "version", "status", "note", "created_at", "updated_at" };

    private final String prefix;

    public EstimateMapper() {
        this("");
    }

    public EstimateMapper(String prefix) {
        this.prefix = prefix;
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public Estimate map(ResultSet rs) throws SQLException {
        int id = rs.getInt(prefix + "id");
        int jobId = rs.getInt(prefix + "job_id");
        int version = rs.getInt(prefix + "version");
        
        String statusStr = rs.getString(prefix + "status");
        EstimateStatus status = statusStr == null ? null : EstimateStatus.valueOf(statusStr);
        
        String note = rs.getString(prefix + "note");
        LocalDateTime createdAt = RowMapper.toLocalDateTime(rs, prefix + "created_at");
        LocalDateTime updatedAt = RowMapper.toLocalDateTime(rs, prefix + "updated_at");

        // Sử dụng Constructor của Estimate để truyền dữ liệu vào trực tiếp (không gọi các hàm set...)[cite: 18]
        return new Estimate(
                id,
                jobId,
                version,
                status,
                note,
                new ArrayList<>(),
                new ArrayList<>(),
                createdAt,
                updatedAt
        );
    }
}