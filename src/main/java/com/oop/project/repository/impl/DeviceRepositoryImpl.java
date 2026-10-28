package com.oop.project.repository.impl;

import com.oop.project.exception.DataAccessException;
import com.oop.project.model.Device;
import com.oop.project.repository.DeviceRepository;
import com.oop.project.repository.mapper.CustomerMapper;
import com.oop.project.repository.mapper.DeviceMapper;
import com.oop.project.repository.mapper.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class DeviceRepositoryImpl extends AbstractJdbcRepository implements DeviceRepository {

    /** Luôn JOIN customers để Device có sẵn Customer. */
    private static final String SELECT = "SELECT " + DeviceMapper.columns("d", "d_") + ", "
            + CustomerMapper.columns("c", "c_")
            + " FROM devices d JOIN customers c ON c.id = d.customer_id";

    private static final String INSERT = "INSERT INTO devices (customer_id, device_type, brand, model, serial_number) "
            + "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";

    private static final String UPDATE = "UPDATE devices SET customer_id = ?, device_type = ?, brand = ?, model = ?, "
            + "serial_number = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String DELETE = "DELETE FROM devices WHERE id = ?";

    private final DeviceMapper mapper = new DeviceMapper();

    public DeviceRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Optional<Device> findById(Integer id) {
        return queryOne("Tìm thiết bị", SELECT + " WHERE d.id = ?",
                ps -> ps.setInt(1, id), mapper);
    }

    @Override
    public List<Device> findAll() {
        return queryList("Lấy danh sách thiết bị", SELECT + " ORDER BY d.id",
                NO_PARAMS, mapper);
    }

    @Override
    public Device save(Device device) {
        requireCustomer(device);
        return queryOne("Thêm thiết bị", INSERT,
                ps -> {
                    ps.setInt(1, device.getCustomer().getId());
                    ps.setString(2, device.getDeviceType());
                    ps.setString(3, device.getBrand());
                    ps.setString(4, device.getModel());
                    ps.setString(5, device.getSerialNumber());
                },
                rs -> {
                    device.setId(rs.getInt("id"));
                    device.setCreatedAt(RowMapper.toLocalDateTime(rs, "created_at"));
                    device.setUpdatedAt(RowMapper.toLocalDateTime(rs, "updated_at"));
                    return device;
                })
                .orElseThrow(() -> new DataAccessException(
                        "Thêm thiết bị thất bại: không nhận được id từ cơ sở dữ liệu"));
    }

    @Override
    public void update(Device device) {
        requireCustomer(device);
        int rows = executeUpdate("Cập nhật thiết bị", UPDATE, ps -> {
            ps.setInt(1, device.getCustomer().getId());
            ps.setString(2, device.getDeviceType());
            ps.setString(3, device.getBrand());
            ps.setString(4, device.getModel());
            ps.setString(5, device.getSerialNumber());
            ps.setInt(6, device.getId());
        });
        requireFound(rows, "thiết bị", device.getId());
    }

    @Override
    public void delete(Integer id) {
        int rows = executeUpdate("Xóa thiết bị", DELETE, ps -> ps.setInt(1, id));
        requireFound(rows, "thiết bị", id);
    }

    @Override
    public List<Device> findByCustomerId(int customerId) {
        return queryList("Lấy thiết bị theo khách hàng",
                SELECT + " WHERE d.customer_id = ? ORDER BY d.id",
                ps -> ps.setInt(1, customerId), mapper);
    }

    private static void requireCustomer(Device device) {
        if (device.getCustomer() == null || device.getCustomer().getId() <= 0) {
            throw new IllegalArgumentException(
                    "Thiết bị phải gắn với một khách hàng đã lưu (customer.id > 0)");
        }
    }
}