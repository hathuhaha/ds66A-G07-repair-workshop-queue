package com.oop.project.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.oop.project.model.Device;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;

/**
 * Dựng RepairJob kèm Device và Customer. Câu SELECT phải JOIN repair_jobs,
 * devices,
 * customers với các tiền tố "j_", "d_", "c_" (xem RepairJobRepositoryImpl).
 */
public class RepairJobMapper implements RowMapper<RepairJob> {

    // device_id không nằm trong danh sách vì Device được dựng từ các cột của bảng
    // devices
    private static final String[] COLUMNS = {
            "id", "issue_description", "status", "desired_days",
            "estimated_ready_at", "closed_at", "created_at", "updated_at" };

    private final String prefix;
    private final DeviceMapper deviceMapper;

    public RepairJobMapper() {
        this("j_", new DeviceMapper());
    }

    public RepairJobMapper(String jobPrefix, DeviceMapper deviceMapper) {
        this.prefix = jobPrefix;
        this.deviceMapper = deviceMapper;
    }

    public static String columns(String tableAlias, String prefix) {
        return RowMapper.selectList(tableAlias, prefix, COLUMNS);
    }

    @Override
    public RepairJob map(ResultSet rs) throws SQLException {
        int id = rs.getInt(prefix + "id");
        Device device = deviceMapper.map(rs);
        String issueDescription = rs.getString(prefix + "issue_description");

        String statusStr = rs.getString(prefix + "status");
        JobStatus status = statusStr == null ? null : JobStatus.valueOf(statusStr);

        int desiredDays = rs.getInt(prefix + "desired_days");
        LocalDateTime estimatedReadyAt = RowMapper.toLocalDateTime(rs, prefix + "estimated_ready_at");
        LocalDateTime closedAt = RowMapper.toLocalDateTime(rs, prefix + "closed_at");
        LocalDateTime createdAt = RowMapper.toLocalDateTime(rs, prefix + "created_at");
        LocalDateTime updatedAt = RowMapper.toLocalDateTime(rs, prefix + "updated_at");

        // Sử dụng Constructor đầy đủ tham số của lớp RepairJob
        return new RepairJob(
                id,
                device,
                issueDescription,
                status,
                desiredDays,
                estimatedReadyAt,
                closedAt,
                createdAt,
                updatedAt);
    }
}