package com.oop.project.repository.mapper;

import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Chỉ dựng phần đầu của báo giá. Danh sách parts và laborServices được nạp
 * riêng
 * (xem EstimateRepositoryImpl) và ở đây khởi tạo rỗng.
 */
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
        Estimate estimate = new Estimate();
        estimate.setId(rs.getInt(prefix + "id"));
        estimate.setJobId(rs.getInt(prefix + "job_id"));
        estimate.setVersion(rs.getInt(prefix + "version"));
        estimate.setStatus(EstimateStatus.valueOf(rs.getString(prefix + "status")));
        estimate.setNote(rs.getString(prefix + "note"));
        estimate.setParts(new ArrayList<>());
        estimate.setLaborServices(new ArrayList<>());
        estimate.setCreatedAt(RowMapper.toLocalDateTime(rs, prefix + "created_at"));
        estimate.setUpdatedAt(RowMapper.toLocalDateTime(rs, prefix + "updated_at"));
        return estimate;
    }
}