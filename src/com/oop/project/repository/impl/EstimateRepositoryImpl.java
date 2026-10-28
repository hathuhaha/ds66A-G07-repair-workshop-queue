package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;
import com.oop.project.model.LaborService;
import com.oop.project.model.Part;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.repository.EstimateRepository;
import com.oop.project.repository.mapper.EstimateMapper;
import com.oop.project.repository.mapper.LaborServiceMapper;
import com.oop.project.repository.mapper.PartMapper;
import com.oop.project.repository.mapper.RowMapper;

public class EstimateRepositoryImpl extends AbstractJdbcRepository implements EstimateRepository {

    private static final String SELECT = "SELECT " + EstimateMapper.columns("e", "") + " FROM estimates e";

    private static final String SELECT_PARTS = "SELECT " + PartMapper.columns("p", "")
            + " FROM parts p WHERE p.estimate_id IN (%s) "
            + "ORDER BY p.id";

    private static final String SELECT_LABOR = "SELECT " + LaborServiceMapper.columns("l", "")
            + " FROM labor_services l "
            + "WHERE l.estimate_id IN (%s) ORDER BY l.id";

    private static final String LOCK_JOB = "SELECT id FROM repair_jobs WHERE id = ? FOR UPDATE";

    private static final String NEXT_VERSION = "SELECT COALESCE(MAX(version), 0) + 1 FROM estimates WHERE job_id = ?";

    private static final String INSERT_ESTIMATE = "INSERT INTO estimates (job_id, version, status, note) VALUES (?, ?, ?, ?) "
            + "RETURNING id, created_at, updated_at";

    private static final String INSERT_PART = "INSERT INTO parts (estimate_id, name, unit_price, quantity) VALUES (?, ?, ?, ?) "
            + "RETURNING id";

    private static final String INSERT_LABOR = "INSERT INTO labor_services (estimate_id, description, hours, hourly_rate) "
            + "VALUES (?, ?, ?, ?) RETURNING id";

    private static final String UPDATE_STATUS = "UPDATE estimates SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String UPDATE_STATUS_OF_JOB = "UPDATE estimates SET status = ?, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = ? AND job_id = ?";

    private final EstimateMapper estimateMapper = new EstimateMapper();
    private final PartMapper partMapper = new PartMapper();
    private final LaborServiceMapper laborMapper = new LaborServiceMapper();

    public EstimateRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    // ------------------------------------------------------------------ read

    @Override
    public Optional<Estimate> findById(int id) {
        return loadEstimates("Find estimate", SELECT + " WHERE e.id = ?",
                ps -> ps.setInt(1, id)).stream().findFirst();
    }

    @Override
    public List<Estimate> findByJobId(int jobId) {
        return loadEstimates("Get estimates by job",
                SELECT + " WHERE e.job_id = ? ORDER BY e.version ASC",
                ps -> ps.setInt(1, jobId));
    }

    @Override
    public Optional<Estimate> findLatestByJobId(int jobId) {
        return loadEstimates("Get latest estimate of job",
                SELECT + " WHERE e.job_id = ? ORDER BY e.version DESC LIMIT 1",
                ps -> ps.setInt(1, jobId)).stream().findFirst();
    }

    /**
     * Reads the estimate headers, then loads their parts and laborServices (on the
     * same connection).
     */
    private List<Estimate> loadEstimates(String action, String sql, StatementSetter setter) {
        try (Connection c = dataSource.getConnection()) {
            List<Estimate> estimates = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                setter.set(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        estimates.add(estimateMapper.map(rs));
                    }
                }
            }
            attachLines(c, estimates);
            return estimates;
        } catch (SQLException e) {
            throw translate(action, e);
        }
    }

    /** Loads the lines of several estimates with 2 queries (no N+1). */
    private void attachLines(Connection c, List<Estimate> estimates) throws SQLException {
        if (estimates.isEmpty()) {
            return;
        }
        Map<Integer, Estimate> byId = new LinkedHashMap<>();
        for (Estimate estimate : estimates) {
            byId.put(estimate.getId(), estimate);
        }
        String in = placeholders(byId.size());

        try (PreparedStatement ps = c.prepareStatement(String.format(SELECT_PARTS, in))) {
            bindIds(ps, byId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Part part = partMapper.map(rs);
                    byId.get(part.getEstimateId()).addPart(part);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(String.format(SELECT_LABOR, in))) {
            bindIds(ps, byId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LaborService labor = laborMapper.map(rs);
                    byId.get(labor.getEstimateId()).addLaborService(labor);
                }
            }
        }
    }

    private static void bindIds(PreparedStatement ps, Map<Integer, Estimate> byId)
            throws SQLException {
        int index = 1;
        for (Integer id : byId.keySet()) {
            ps.setInt(index++, id);
        }
    }

    // ------------------------------------------------------------------ write

    /**
     * Saves the header and the lines in one transaction: either everything is
     * stored or nothing is. The version is always assigned here (see
     * insertEstimate); this method only inserts new estimates (id must be 0).
     */
    @Override
    public Estimate save(Estimate estimate) {
        if (estimate == null || estimate.getJobId() <= 0) {
            throw new IllegalArgumentException(
                    "Estimate must belong to a saved repair job (jobId > 0)");
        }
        if (estimate.getId() != 0) {
            throw new IllegalArgumentException(
                    "Estimate has already been saved (id must be 0)");
        }
        inTransaction("Save estimate", c -> insertEstimate(c, estimate));
        return estimate;
    }

    private void insertEstimate(Connection c, Estimate estimate) throws SQLException {
        int jobId = estimate.getJobId();

        // Lock the job row so two concurrent callers cannot get the same version
        try (PreparedStatement ps = c.prepareStatement(LOCK_JOB)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new EntityNotFoundException(
                            "Repair job not found with id = " + jobId);
                }
            }
        }

        // The repository always assigns the version: highest version of the job + 1.
        // Any version already set on the estimate (the model default is 1) is
        // overwritten.
        try (PreparedStatement ps = c.prepareStatement(NEXT_VERSION)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                estimate.setVersion(rs.getInt(1));
            }
        }
        if (estimate.getStatus() == null) {
            estimate.setStatus(EstimateStatus.DRAFT);
        }

        try (PreparedStatement ps = c.prepareStatement(INSERT_ESTIMATE)) {
            ps.setInt(1, jobId);
            ps.setInt(2, estimate.getVersion());
            ps.setString(3, estimate.getStatus().name());
            ps.setString(4, estimate.getNote());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                estimate.setId(rs.getInt("id"));
                estimate.setCreatedAt(RowMapper.toLocalDateTime(rs, "created_at"));
                estimate.setUpdatedAt(RowMapper.toLocalDateTime(rs, "updated_at"));
            }
        }

        List<Part> parts = estimate.getParts() == null
                ? Collections.<Part>emptyList()
                : estimate.getParts();
        try (PreparedStatement ps = c.prepareStatement(INSERT_PART)) {
            for (Part part : parts) {
                ps.setInt(1, estimate.getId());
                ps.setString(2, part.getName());
                ps.setBigDecimal(3, part.getUnitPrice());
                ps.setInt(4, part.getQuantity());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    part.setId(rs.getInt(1));
                }
                part.setEstimateId(estimate.getId());
            }
        }

        List<LaborService> labors = estimate.getLaborServices() == null
                ? Collections.<LaborService>emptyList()
                : estimate.getLaborServices();
        try (PreparedStatement ps = c.prepareStatement(INSERT_LABOR)) {
            for (LaborService labor : labors) {
                ps.setInt(1, estimate.getId());
                ps.setString(2, labor.getDescription());
                ps.setBigDecimal(3, labor.getHours());
                ps.setBigDecimal(4, labor.getHourlyRate());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    labor.setId(rs.getInt(1));
                }
                labor.setEstimateId(estimate.getId());
            }
        }
    }

    @Override
    public void updateStatus(int estimateId, EstimateStatus status) {
        Objects.requireNonNull(status, "status");
        int rows = executeUpdate("Update estimate status", UPDATE_STATUS, ps -> {
            ps.setString(1, status.name());
            ps.setInt(2, estimateId);
        });
        if (rows == 0) {
            throw new EntityNotFoundException("Estimate not found with id = " + estimateId);
        }
    }

    @Override
    public void updateEstimateStatusAndJob(int estimateId, EstimateStatus status,
            RepairJob job, StatusHistory history) {
        Objects.requireNonNull(status, "status");
        JobWrites.requireJobAndHistory(job, history);
        inTransaction("Update estimate and repair job", c -> {
            try (PreparedStatement ps = c.prepareStatement(UPDATE_STATUS_OF_JOB)) {
                ps.setString(1, status.name());
                ps.setInt(2, estimateId);
                ps.setInt(3, job.getId());
                if (ps.executeUpdate() == 0) {
                    throw new EntityNotFoundException("Estimate not found with id = "
                            + estimateId + " for job with id = " + job.getId());
                }
            }
            JobWrites.updateJobStatus(c, job);
            JobWrites.insertHistory(c, history);
        });
    }
}