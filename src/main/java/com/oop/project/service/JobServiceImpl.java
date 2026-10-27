package com.oop.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.exception.InvalidStatusTransitionException;

import com.oop.project.repository.DeviceRepository;
import com.oop.project.repository.EstimateRepository;
import com.oop.project.repository.RepairJobRepository;
import com.oop.project.repository.StatusHistoryRepository;

import com.oop.project.model.Device;
import com.oop.project.model.Estimate;
import com.oop.project.model.LaborService;
import com.oop.project.model.Part;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.model.JobStatus;

public class JobServiceImpl implements JobService {
    private final DeviceRepository deviceRepository;
    private final RepairJobRepository repairJobRepository;
    private final EstimateRepository estimateRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public JobServiceImpl(DeviceRepository deviceRepository, RepairJobRepository repairJobRepository,
            EstimateRepository estimateRepository, StatusHistoryRepository statusHistoryRepository) {
        this.deviceRepository = deviceRepository;
        this.repairJobRepository = repairJobRepository;
        this.estimateRepository = estimateRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    private RepairJob findJobOrThrow(int jobId) {
        return repairJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find order " + jobId));
    }

    private Estimate findEstimateOrThrow(int estimatedId) {
        return estimateRepository.findById(estimatedId)
                .orElseThrow(() -> new EntityNotFoundException("No quotation found with ID " + estimatedId));
    }

    @Override
    public void updateEstimatedReadyAt(int jobId, LocalDateTime estimatedReadyAt) {
        RepairJob job = findJobOrThrow(jobId);
        job.setEstimatedReadyAt(estimatedReadyAt);
        repairJobRepository.update(job);
    }

    @Override
    public Estimate createEstimate(int jobId, List<Part> parts, List<LaborService> laborServices, String note) {
        findJobOrThrow(jobId);

        int nextVersion;
        Optional<Estimate> latest = estimateRepository.findLatestByJobId(jobId);
        if (latest.isPresent()) {
            nextVersion = latest.get().getVersion() + 1;
        } else {
            nextVersion = 1;
        }

        Estimate estimate = new Estimate(jobId, note, parts, laborServices);
        estimate.setVersion(nextVersion);

        return estimateRepository.save(estimate);
    }

    private void validateTransition(RepairJob job, JobStatus newStatus) {
        JobStatus current = job.getStatus();

        if (!current.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status from " + current.name()
                            + " to " + newStatus.name());
        }

        Optional<Estimate> latest = estimateRepository.findLatestByJobId(job.getId());

        if (newStatus == JobStatus.REPAIRING) {
            boolean approved = latest.map(Estimate::isApproved).orElse(false);

            if (!approved) {
                throw new InvalidStatusTransitionException(
                        "No quotation has been approved by the customer yet");
            }
        }

        if (newStatus == JobStatus.CLOSED
                && current == JobStatus.WAITING_FOR_APPROVAL) {
            boolean rejected = latest.map(Estimate::isRejected).orElse(false);

            if (!rejected) {
                throw new InvalidStatusTransitionException(
                        "Order can only be closed after the customer has rejected the quotation");
            }
        }
    }

    @Override
    public void updateJobStatusWithHistory(int jobId, JobStatus newStatus, String note) {
        RepairJob job = findJobOrThrow(jobId);
        JobStatus oldStatus = job.getStatus();

        validateTransition(job, newStatus);

        job.setStatus(newStatus);
        if (newStatus == JobStatus.CLOSED) {
            job.setClosedAt(LocalDateTime.now());
        }

        StatusHistory history = new StatusHistory(jobId, oldStatus, newStatus, note);

        // TODO: chờ M2 cung cấp hàm lưu job + history trong một transaction
        throw new UnsupportedOperationException("");
    }

    @Override
    public RepairJob createJob(int deviceId, String issueDescription, int desiredDays) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find device with id " + deviceId));

        RepairJob job = new RepairJob(device, issueDescription, desiredDays);

        // TODO: chờ M2: lưu job + ghi history đầu tiên (oldStatus = null, newStatus =
        // RECEIVED)
        // trong một transaction, trả về job đã có id. M2 tự tạo history sau khi có id.
        throw new UnsupportedOperationException("");
    }

    @Override
    public void approveEstimate(int estimateId) {
        Estimate estimate = findEstimateOrThrow(estimateId);
        RepairJob job = findJobOrThrow(estimate.getJobId());

        checkIsLatestEstimate(estimate);

        if (!estimate.isDraft()) {
            throw new InvalidStatusTransitionException("Only quotations with the **DRAFT** status can be approved");
        }

        JobStatus oldStatus = job.getStatus();

        // Kiểm tra luật chuyển trạng thái job sang REPAIRING.
        // validateTransition đọc estimate mới nhất từ DB, lúc này vẫn là DRAFT nên sẽ
        // báo lỗi.
        // Vì vậy kiểm tra thủ công bằng canTransitionTo thay vì gọi validateTransition.
        if (!oldStatus.canTransitionTo(JobStatus.REPAIRING)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status from " + oldStatus.name() + " to " + JobStatus.REPAIRING.name());
        }

        job.setStatus(JobStatus.REPAIRING);
        StatusHistory history = new StatusHistory(job.getId(), oldStatus, JobStatus.REPAIRING,
                "Customer approves the quotation");

        // TODO: chờ M2: updateStatus estimate -> APPROVED + update job + ghi history,
        // một transaction
        throw new UnsupportedOperationException("");
    }

    @Override
    public void rejectEstimate(int estimateId, String note) {
        Estimate estimate = findEstimateOrThrow(estimateId);
        RepairJob job = findJobOrThrow(estimate.getJobId());

        checkIsLatestEstimate(estimate);

        if (!estimate.isDraft()) {
            throw new InvalidStatusTransitionException("Only quotations with the DRAFT status can be rejected");
        }

        JobStatus oldStatus = job.getStatus();

        if (!oldStatus.canTransitionTo(JobStatus.CLOSED) || oldStatus != JobStatus.WAITING_FOR_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "Only orders with the WAITING_FOR_APPROVAL status can have their quotations rejected");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());

        String historyNote = (note == null || note.isBlank()) ? "Customer rejects the quotation" : note;
        StatusHistory history = new StatusHistory(job.getId(), oldStatus, JobStatus.CLOSED, historyNote);

        // TODO: chờ M2: updateStatus estimate -> REJECTED + update job + ghi history,
        // một transaction
        throw new UnsupportedOperationException("");
    }

    private void checkIsLatestEstimate(Estimate estimate) {
        Optional<Estimate> latest = estimateRepository.findLatestByJobId(estimate.getJobId());
        if (latest.isEmpty() || latest.get().getId() != estimate.getId()) {
            throw new InvalidStatusTransitionException("Only the latest quotation for an order can be processed");
        }
    }
}
