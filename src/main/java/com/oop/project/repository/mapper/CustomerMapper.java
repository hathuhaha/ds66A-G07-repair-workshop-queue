package com.oop.project.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.oop.project.model.Customer;

public class CustomerMapper implements RowMapper<Customer> {

    private static final String[] COLUMNS = { "id", "full_name", "phone", "email", "address", "created_at",
            "updated_at" };

    private final String prefix;

    /** Dùng khi truy vấn riêng bảng customers (không có tiền tố). */
    public CustomerMapper() {
        this("");
    }

    /** Dùng khi customers được JOIN kèm bảng khác (ví dụ tiền tố "c_"). */
    public CustomerMapper(String prefix) {
        this.prefix = prefix;
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public Customer map(ResultSet rs) throws SQLException {
        int id = rs.getInt(prefix + "id");
        String fullName = rs.getString(prefix + "full_name");
        String phone = rs.getString(prefix + "phone");
        String email = rs.getString(prefix + "email");
        String address = rs.getString(prefix + "address");
        LocalDateTime createdAt = RowMapper.toLocalDateTime(rs, prefix + "created_at");
        LocalDateTime updatedAt = RowMapper.toLocalDateTime(rs, prefix + "updated_at");

        // Gọi constructor đầy đủ tham số của Customer
        Customer customer = new Customer(id, fullName, phone, email, address, createdAt, updatedAt);
        return customer;
    }
}