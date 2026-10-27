package com.oop.project.repository.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oop.project.model.Part;

public class PartMapper implements RowMapper<Part> {

    private static final String[] COLUMNS = { "id", "estimate_id", "name", "unit_price", "quantity" };

    private final String prefix;

    public PartMapper() {
        this("");
    }

    public PartMapper(String prefix) {
        this.prefix = prefix;
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public Part map(ResultSet rs) throws SQLException {
        int id = rs.getInt(prefix + "id");
        int estimateId = rs.getInt(prefix + "estimate_id");
        String name = rs.getString(prefix + "name");
        BigDecimal unitPrice = rs.getBigDecimal(prefix + "unit_price");
        int quantity = rs.getInt(prefix + "quantity");

        return new Part(id, estimateId, name, unitPrice, quantity);
    }
}