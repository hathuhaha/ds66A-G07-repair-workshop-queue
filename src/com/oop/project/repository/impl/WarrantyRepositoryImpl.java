package com.oop.project.repository.impl;

import com.oop.project.exception.DataAccessException;
import com.oop.project.model.LegacyWarrantyImport;
import com.oop.project.model.WarrantyCandidate;
import com.oop.project.model.WarrantyRecord;
import com.oop.project.repository.WarrantyRepository;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class WarrantyRepositoryImpl extends AbstractJdbcRepository implements WarrantyRepository {

    private static final String INSERT_NATIVE = """
            INSERT INTO warranties (repair_job_id, device_id, start_date, end_date, source, note)
            SELECT j.id, j.device_id, ?, ?, 'NATIVE', NULL
            FROM repair_jobs j
            WHERE j.id = ?
              AND j.status IN ('READY', 'CLOSED')
              AND NOT EXISTS (
                  SELECT 1 FROM warranties w WHERE w.repair_job_id = j.id
              )
            RETURNING id
            """;

    public WarrantyRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<WarrantyRecord> findAll() {
        String sql = """
                SELECT warranty_id, repair_id, serial_number, device_name, customer_name,
                       start_date, end_date, source, warranty_status
                FROM v_warranty_list
                ORDER BY end_date DESC, warranty_id DESC
                """;
        return queryList("Lấy danh sách bảo hành", sql, NO_PARAMS, rs -> new WarrantyRecord(
                rs.getInt("warranty_id"),
                (Integer) rs.getObject("repair_id"),
                rs.getString("serial_number"),
                rs.getString("device_name"),
                rs.getString("customer_name"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getString("source"),
                rs.getString("warranty_status")));
    }

    @Override
    public List<WarrantyCandidate> findCandidates() {
        String sql = """
                SELECT repair_id, serial_number, device_name, customer_name, received_at, completed_at
                FROM v_warranty_candidates
                ORDER BY completed_at DESC NULLS LAST, repair_id DESC
                """;
        return queryList("Lấy đơn đủ điều kiện tạo bảo hành", sql, NO_PARAMS, rs -> new WarrantyCandidate(
                rs.getInt("repair_id"),
                rs.getString("serial_number"), // Đổi từ getInt("device_id") sang getString("serial_number")
                rs.getString("device_name"),
                rs.getString("customer_name"),
                rs.getTimestamp("received_at").toLocalDateTime(),
                rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime()));
    }

    @Override
    public WarrantyRecord createForRepair(int repairId, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        Optional<Integer> createdId = queryOne("Tạo bảo hành", INSERT_NATIVE, ps -> {
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ps.setInt(3, repairId);
        }, rs -> rs.getInt("id"));
        int warrantyId = createdId.orElseThrow(
                () -> new DataAccessException("Không tìm thấy đơn đủ điều kiện tạo bảo hành, "
                        + "hoặc đơn đã có bảo hành."));
        return findAll().stream()
                .filter(warranty -> warranty.getWarrantyId() == warrantyId)
                .findFirst()
                .orElseThrow(() -> new DataAccessException("Đã tạo bảo hành nhưng không thể tải lại dữ liệu."));
    }

    @Override
    public int importLegacy(List<LegacyWarrantyImport> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        int[] imported = { 0 };
        inTransaction("Nhập dữ liệu bảo hành cũ", connection -> {
            String findDeviceSql = "SELECT id FROM devices WHERE serial_number = ?";
            String insertSql = """
                    INSERT INTO warranties (device_id, start_date, end_date, source, legacy_ref, note)
                    VALUES (?, ?, ?, 'LEGACY', ?, ?)
                    ON CONFLICT DO NOTHING
                    """;

            try (PreparedStatement findDeviceStmt = connection.prepareStatement(findDeviceSql);
                    PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                for (LegacyWarrantyImport record : records) {
                    validateDates(record.getStartDate(), record.getEndDate());

                    Integer deviceId = null;
                    findDeviceStmt.setString(1, record.getSerialNumber());
                    try (ResultSet rs = findDeviceStmt.executeQuery()) {
                        if (rs.next()) {
                            deviceId = rs.getInt("id");
                        }
                    }

                    if (deviceId == null) {
                        continue;
                    }

                    insertStmt.setInt(1, deviceId);
                    insertStmt.setDate(2, Date.valueOf(record.getStartDate()));
                    insertStmt.setDate(3, Date.valueOf(record.getEndDate()));
                    insertStmt.setString(4, record.getLegacyRef());
                    if (record.getNote() == null) {
                        insertStmt.setNull(5, Types.VARCHAR);
                    } else {
                        insertStmt.setString(5, record.getNote());
                    }
                    insertStmt.addBatch();
                }

                for (int result : insertStmt.executeBatch()) {
                    if (result == Statement.EXECUTE_FAILED) {
                        throw new SQLException("A legacy warranty record could not be inserted.");
                    }
                    if (result > 0 || result == Statement.SUCCESS_NO_INFO) {
                        imported[0]++;
                    }
                }
            }
        });
        return imported[0];
    }

    private static void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Warranty end date must be on or after its start date.");
        }
    }
}