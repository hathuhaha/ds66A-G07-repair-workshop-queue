package com.oop.project.repository.impl;

import com.oop.project.exception.DataAccessException;
import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.repository.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Các hàm dùng chung cho mọi repository JDBC: mở/đóng connection, chạy truy
 * vấn,
 * đổi SQLException thành DataAccessException.
 * Mỗi lần gọi lấy một connection từ pool và trả lại ngay khi xong
 * (try-with-resources).
 */
abstract class AbstractJdbcRepository {

    protected final DataSource dataSource;

    protected AbstractJdbcRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /** Gán tham số cho PreparedStatement. */
    @FunctionalInterface
    protected interface StatementSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    protected static final StatementSetter NO_PARAMS = ps -> {
    };

    protected <T> List<T> queryList(String action, String sql, StatementSetter setter,
            RowMapper<T> mapper) {
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw translate(action, e);
        }
    }

    /** Lấy dòng đầu tiên (hoặc rỗng). Dùng cả cho INSERT ... RETURNING. */
    protected <T> Optional<T> queryOne(String action, String sql, StatementSetter setter,
            RowMapper<T> mapper) {
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw translate(action, e);
        }
    }

    /** Chạy INSERT/UPDATE/DELETE, trả về số dòng bị ảnh hưởng. */
    protected int executeUpdate(String action, String sql, StatementSetter setter) {
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw translate(action, e);
        }
    }

    /**
     * Ném EntityNotFoundException nếu câu UPDATE/DELETE không tác động dòng nào.
     */
    protected void requireFound(int affectedRows, String what, int id) {
        if (affectedRows == 0) {
            throw new EntityNotFoundException("Không tìm thấy " + what + " có id = " + id);
        }
    }

    /**
     * Đổi SQLException thành DataAccessException với thông báo tiếng Việt dễ hiểu.
     */
    protected DataAccessException translate(String action, SQLException e) {
        String detail;
        String state = e.getSQLState();
        if ("23503".equals(state)) {
            detail = "vi phạm khóa ngoại (dữ liệu liên quan còn tồn tại hoặc không tồn tại)";
        } else if ("23505".equals(state)) {
            detail = "dữ liệu bị trùng";
        } else if ("23502".equals(state)) {
            detail = "thiếu dữ liệu bắt buộc";
        } else if ("23514".equals(state)) {
            detail = "dữ liệu không thỏa ràng buộc của cơ sở dữ liệu";
        } else {
            detail = e.getMessage();
        }
        return new DataAccessException(action + " thất bại: " + detail, e);
    }

    protected static void setDateTime(PreparedStatement ps, int index, LocalDateTime value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    /** Sinh "?, ?, ?" cho mệnh đề IN. */
    protected static String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('?');
        }
        return sb.toString();
    }

    /** Thoát các ký tự đặc biệt của LIKE (dùng với ESCAPE '\'). */
    protected static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Công việc chạy bên trong một transaction trên cùng một connection. */
    @FunctionalInterface
    protected interface TransactionWork {
        void run(Connection connection) throws SQLException;
    }

    /**
     * Chạy work trong một transaction: tất cả thành công thì commit, có lỗi thì
     * rollback
     * (không để lại gì). Luôn trả autoCommit về true và trả connection về pool.
     * SQLException được đổi thành DataAccessException; RuntimeException (ví dụ
     * EntityNotFoundException) được ném lại nguyên vẹn sau khi rollback.
     */
    protected void inTransaction(String action, TransactionWork work) {
        Connection c = null;
        try {
            c = dataSource.getConnection();
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(c);
                throw e;
            }
        } catch (SQLException e) {
            throw translate(action, e);
        } finally {
            closeQuietly(c);
        }
    }

    private static void rollbackQuietly(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // không che mất lỗi gốc
        }
    }

    /** Trả autoCommit về true rồi đóng (trả connection về pool). */
    private static void closeQuietly(Connection c) {
        if (c == null) {
            return;
        }
        try {
            c.setAutoCommit(true);
        } catch (SQLException ignored) {
            // bỏ qua
        }
        try {
            c.close();
        } catch (SQLException ignored) {
            // bỏ qua
        }
    }
}