package com.oop.project.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.oop.project.model.Customer;
import com.oop.project.model.Device;

/**
 * Dựng Device kèm Customer. Câu SELECT phải JOIN customers và dùng
 * DeviceMapper.columns(...) cùng CustomerMapper.columns(...) với đúng các tiền
 * tố.
 */
public class DeviceMapper implements RowMapper<Device> {

    // customer_id không nằm trong danh sách vì Customer được dựng từ các cột của
    // bảng customers
    private static final String[] COLUMNS = { "id", "device_type", "brand", "model", "serial_number", "created_at",
            "updated_at" };

    private final String prefix;
    private final CustomerMapper customerMapper;

    /** Mặc định: cột thiết bị có tiền tố "d_", cột khách hàng có tiền tố "c_". */
    public DeviceMapper() {
        this("d_", "c_");
    }

    public DeviceMapper(String devicePrefix, String customerPrefix) {
        this.prefix = devicePrefix;
        this.customerMapper = new CustomerMapper(customerPrefix);
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public Device map(ResultSet rs) throws SQLException {
        int id = rs.getInt(prefix + "id");
        Customer customer = customerMapper.map(rs);
        String deviceType = rs.getString(prefix + "device_type");
        String brand = rs.getString(prefix + "brand");
        String model = rs.getString(prefix + "model");
        String serialNumber = rs.getString(prefix + "serial_number");
        LocalDateTime createdAt = RowMapper.toLocalDateTime(rs, prefix + "created_at");
        LocalDateTime updatedAt = RowMapper.toLocalDateTime(rs, prefix + "updated_at");

        // Gọi constructor đầy đủ tham số của Device
        Device device = new Device(id, customer, deviceType, brand, model, serialNumber, createdAt, updatedAt);
        return device;
    }
}