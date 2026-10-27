package com.oop.project.repository.impl;

import com.oop.project.exception.DataAccessException;
import com.oop.project.model.Customer;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.repository.mapper.CustomerMapper;
import com.oop.project.repository.mapper.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class CustomerRepositoryImpl extends AbstractJdbcRepository implements CustomerRepository {

    private static final String SELECT = "SELECT " + CustomerMapper.columns("c", "") + " FROM customers c";

    private static final String INSERT = "INSERT INTO customers (full_name, phone, email, address) VALUES (?, ?, ?, ?) "
            + "RETURNING id, created_at, updated_at";

    private static final String UPDATE = "UPDATE customers SET full_name = ?, phone = ?, email = ?, address = ?, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String DELETE = "DELETE FROM customers WHERE id = ?";

    private final CustomerMapper mapper = new CustomerMapper();

    public CustomerRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        return queryOne("Tìm khách hàng", SELECT + " WHERE c.id = ?",
                ps -> ps.setInt(1, id), mapper);
    }

    @Override
    public List<Customer> findAll() {
        return queryList("Lấy danh sách khách hàng", SELECT + " ORDER BY c.id",
                NO_PARAMS, mapper);
    }

    @Override
    public Customer save(Customer customer) {
        return queryOne("Thêm khách hàng", INSERT,
                ps -> {
                    ps.setString(1, customer.getFullName());
                    ps.setString(2, customer.getPhone());
                    ps.setString(3, customer.getEmail());
                    ps.setString(4, customer.getAddress());
                },
                rs -> {
                    customer.setId(rs.getInt("id"));
                    customer.setCreatedAt(RowMapper.toLocalDateTime(rs, "created_at"));
                    customer.setUpdatedAt(RowMapper.toLocalDateTime(rs, "updated_at"));
                    return customer;
                })
                .orElseThrow(() -> new DataAccessException(
                        "Thêm khách hàng thất bại: không nhận được id từ cơ sở dữ liệu"));
    }

    @Override
    public void update(Customer customer) {
        int rows = executeUpdate("Cập nhật khách hàng", UPDATE, ps -> {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setInt(5, customer.getId());
        });
        requireFound(rows, "khách hàng", customer.getId());
    }

    @Override
    public void delete(Integer id) {
        int rows = executeUpdate("Xóa khách hàng", DELETE, ps -> ps.setInt(1, id));
        requireFound(rows, "khách hàng", id);
    }

    @Override
    public List<Customer> findByPhone(String phone) {
        return queryList("Tìm khách hàng theo số điện thoại",
                SELECT + " WHERE c.phone = ? ORDER BY c.id",
                ps -> ps.setString(1, phone), mapper);
    }

    @Override
    public List<Customer> searchByName(String keyword) {
        // Từ khóa trống thì trả về toàn bộ danh sách (phù hợp với ô tìm kiếm để trống)
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String pattern = "%" + escapeLike(keyword.trim()) + "%";
        return queryList("Tìm khách hàng theo tên",
                SELECT + " WHERE c.full_name ILIKE ? ESCAPE '\\' ORDER BY c.full_name, c.id",
                ps -> ps.setString(1, pattern), mapper);
    }
}