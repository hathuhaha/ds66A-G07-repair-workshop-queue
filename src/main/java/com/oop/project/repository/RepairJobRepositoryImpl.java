package com.oop.project.repository.impl;

import com.oop.project.exception.DataAccessException;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.repository.RepairJobRepository;
import com.oop.project.repository.mapper.CustomerMapper;
import com.oop.project.repository.mapper.DeviceMapper;
import com.oop.project.repository.mapper.RepairJobMapper;
import com.oop.project.repository.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class RepairJobRepositoryImpl extends AbstractJdbcRepository implements RepairJobRepository {

    /**
     * Một câu JOIN dựng sẵn RepairJob kèm Device và Customer (tránh truy vấn N+1).
     */
    private static final String SELECT_JOINED = "SELECT " + RepairJobMapper.columns("j", "j_")
            + ", " + DeviceMapper.columns("d", "d_")
            + ", " + CustomerMapper.columns("c", "c_")
            + " FROM repair_jobs j"
            + " JOIN devices d ON d.id = j.device_id"
            + " JOIN customers c ON c.id = d.customer_id";

    /** Thứ tự hàng đợi (contract 5.1): hạn chót, rồi ngày tiếp nhận, rồi id. */
    private static final String QUEUE_ORDER = " ORDER BY (j.created_at + j.desired_days * INTERVAL '1 day') ASC,"
            + " j.created_at ASC, j.id ASC";

    private static final String INSERT = "INSERT INTO repair_jobs (device_id, issue_description, status, desired_days, "
            + "estimated_ready_at, closed_at) VALUES (?, ?, ?, ?, ?, ?) "
            + "RETURNING id, created_at, updated_at";

    private static final String UPDATE = "UPDATE repair_jobs SET device_id = ?, issue_description = ?, status = ?, "
            + "desired_days = ?, estimated_ready_at = ?, closed_at = ?, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String DELETE = "DELETE FROM repair_jobs WHERE id = ?";

    private final RepairJobMapper mapper = new RepairJobMapper();

    public RepairJobRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Optional<RepairJob> findById(Integer id) {
        return queryOne("Tìm đơn sửa chữa", SELECT_JOINED + " WHERE j.id = ?",
                ps -> ps.setInt(1, id), mapper);
    }

    @Override
    public List<RepairJob> findAll() {
        return queryList("Lấy danh sách đơn sửa chữa", SELECT_JOINED + " ORDER BY j.id",
                NO_PARAMS, mapper);
    }

    @Override
    public RepairJob save(RepairJob job) {
        requireDevice(job);
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.RECEIVED);
        }
        return queryOne("Thêm đơn sửa chữa", INSERT,
                ps -> bind(ps, job),
                rs -> {
                    job.setId(rs.getInt("id"));
                    job.setCreatedAt(RowMapper.toLocalDateTime(rs, "created_at"));
                    job.setUpdatedAt(RowMapper.toLocalDateTime(rs, "updated_at"));
                    return job;
                })
                .orElseThrow(() -> new DataAccessException(
                        "Thêm đơn sửa chữa thất bại: không nhận được id từ cơ sở dữ liệu"));
    }

    @Override
    public void update(RepairJob job) {
        requireDevice(job);
        if (job.getStatus() == null) {
            throw new IllegalArgumentException("Đơn sửa chữa phải có trạng thái");
        }
        int rows = executeUpdate("Cập nhật đơn sửa chữa", UPDATE, ps -> {
            bind(ps, job);
            ps.setInt(7, job.getId());
        });
        requireFound(rows, "đơn sửa chữa", job.getId());
    }

    @Override
    public void delete(Integer id) {
        int rows = executeUpdate("Xóa đơn sửa chữa", DELETE, ps -> ps.setInt(1, id));
        requireFound(rows, "đơn sửa chữa", id);
    }

    @Override
    public List<RepairJob> findByStatus(JobStatus status) {
        return queryList("Lấy đơn theo trạng thái",
                SELECT_JOINED + " WHERE j.status = ? ORDER BY j.id",
                ps -> ps.setString(1, status.name()), mapper);
    }

    @Override
    public List<RepairJob> findByDeviceId(int deviceId) {
        return queryList("Lấy đơn theo thiết bị",
                SELECT_JOINED + " WHERE j.device_id = ? ORDER BY j.id",
                ps -> ps.setInt(1, deviceId), mapper);
    }

    @Override
    public List<RepairJob> findByCustomerId(int customerId) {
        return queryList("Lấy đơn theo khách hàng",
                SELECT_JOINED + " WHERE d.customer_id = ? ORDER BY j.id",
                ps -> ps.setInt(1, customerId), mapper);
    }

    @Override
    public List<RepairJob> findQueue() {
        return findQueue(EnumSet.complementOf(EnumSet.of(JobStatus.CLOSED)));
    }

    @Override
    public List<RepairJob> findQueue(Collection<JobStatus> statuses) {
        // Bỏ null và CLOSED; không còn trạng thái nào thì trả về danh sách rỗng
        EnumSet<JobStatus> wanted = EnumSet.noneOf(JobStatus.class);
        if (statuses != null) {
            for (JobStatus status : statuses) {
                if (status != null && status != JobStatus.CLOSED) {
                    wanted.add(status);
                }
            }
        }
        if (wanted.isEmpty()) {
            return new ArrayList<>();
        }
        String sql = SELECT_JOINED
                + " WHERE j.status IN (" + placeholders(wanted.size()) + ")"
                + QUEUE_ORDER;
        return queryList("Lấy hàng đợi", sql, ps -> {
            int index = 1;
            for (JobStatus status : wanted) {
                ps.setString(index++, status.name());
            }
        }, mapper);
    }

    /** Gán 6 tham số đầu, dùng chung cho INSERT và UPDATE. */
    private void bind(PreparedStatement ps, RepairJob job) throws SQLException {
        ps.setInt(1, job.getDevice().getId());
        ps.setString(2, job.getIssueDescription());
        ps.setString(3, job.getStatus().name());
        ps.setInt(4, job.getDesiredDays());
        setDateTime(ps, 5, job.getEstimatedReadyAt());
        setDateTime(ps, 6, job.getClosedAt());
    }

    private static void requireDevice(RepairJob job) {
        if (job.getDevice() == null || job.getDevice().getId() <= 0) {
            throw new IllegalArgumentException(
                    "Đơn sửa chữa phải gắn với một thiết bị đã lưu (device.id > 0)");
        }
    }
}