package com.oop.project.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Chuyển một dòng ResultSet thành một đối tượng.
 */
@FunctionalInterface
public interface RowMapper<T> {

    T map(ResultSet rs) throws SQLException;

    /**
     * Sinh danh sách cột cho câu SELECT, ví dụ
     * selectList("c", "c_", "id", "phone") -> "c.id AS c_id, c.phone AS c_phone".
     * Dùng chung với mapper để tên cột trong SQL và trong mapper luôn khớp nhau.
     */
    static String selectList(String tableAlias, String prefix, String... columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(tableAlias).append('.').append(columns[i])
                    .append(" AS ").append(prefix).append(columns[i]);
        }
        return sb.toString();
    }

    /** Đọc cột TIMESTAMP, trả null nếu giá trị trong DB là NULL. */
    static LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }
}