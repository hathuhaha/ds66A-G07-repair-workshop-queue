package com.oop.project.repository.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oop.project.model.LaborService;

public class LaborServiceMapper implements RowMapper<LaborService> {

    private static final String[] COLUMNS = { "id", "estimate_id", "description", "hours", "hourly_rate" };

    private final String prefix;

    public LaborServiceMapper() {
        this("");
    }

    public LaborServiceMapper(String prefix) {
        this.prefix = prefix;
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public LaborService map(ResultSet rs) throws SQLException {
        // Đọc dữ liệu từ ResultSet ra các biến tạm
        int id = rs.getInt(prefix + "id");
        int estimateId = rs.getInt(prefix + "estimate_id");
        String description = rs.getString(prefix + "description");
        BigDecimal hours = rs.getBigDecimal(prefix + "hours");
        BigDecimal hourlyRate = rs.getBigDecimal(prefix + "hourly_rate");

        // Khởi tạo đối tượng LaborService bằng Constructor đầy đủ tham số
        LaborService labor = new LaborService(id, estimateId, description, hours, hourlyRate);

        return labor;
    }
}