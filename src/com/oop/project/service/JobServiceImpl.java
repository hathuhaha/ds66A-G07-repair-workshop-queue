package com.oop.project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.exception.InvalidStatusTransitionException;

import com.oop.project.repository.DeviceRepository;
import com.oop.project.repository.EstimateRepository;
import com.oop.project.repository.RepairJobRepository;

import com.oop.project.model.Device;
import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;
import com.oop.project.model.LaborService;
import com.oop.project.model.Part;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.model.JobStatus;

public class JobServiceImpl implements JobService {
    private final DeviceRepository deviceRepository;
    private final RepairJobRepository repairJobRepository;
    private final EstimateRepository estimateRepository;

    public JobServiceImpl(DeviceRepository deviceRepository, RepairJobRepository repairJobRepository,
            EstimateRepository estimateRepository) {
        this.deviceRepository = deviceRepository;
        this.repairJobRepository = repairJobRepository;
        this.estimateRepository = estimateRepository;
    }

    private RepairJob findJobOrThrow(int jobId) {
        return repairJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find order " + jobId));
    }

    private Estimate findEstimateOrThrow(int estimatedId) {
        return estimateRepository.findById(estimatedId)
                .orElseThrow(() -> new EntityNotFoundException("No estimate found with ID " + estimatedId));
    }

    @Override
    public List<JobStatus> getAvailableNextStatuses(int jobId) {
        RepairJob job = findJobOrThrow(jobId);
        List<JobStatus> available = new ArrayList<>();
        for (JobStatus next : JobStatus.values()) {
            if (!job.getStatus().canTransitionTo(next)) {
                continue;
            }
            if (next == JobStatus.WAITING_FOR_APPROVAL) {
                boolean hasDraft = estimateRepository.findLatestByJobId(jobId)
                        .map(Estimate::isDraft)
                        .orElse(false);
                if (!hasDraft) {
                    continue;
                }
            }
            if (next == JobStatus.REPAIRING
                    || (next == JobStatus.CLOSED && job.getStatus() == JobStatus.WAITING_FOR_APPROVAL)) {
                continue;
            }
            available.add(next);
        }
        return List.copyOf(available);
    }

    @Override
    public void updateEstimatedReadyAt(int jobId, LocalDateTime estimatedReadyAt) {
        RepairJob job = findJobOrThrow(jobId);
        job.setEstimatedReadyAt(estimatedReadyAt);
        repairJobRepository.update(job);
    }

    @Override
    public Estimate createEstimate(int jobId, List<Part> parts, List<LaborService> laborServices, String note) {
        RepairJob job = findJobOrThrow(jobId);
        if (job.getStatus() != JobStatus.DIAGNOSING
                && job.getStatus() != JobStatus.WAITING_FOR_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "Estimates can only be created while a repair is being diagnosed or awaiting approval.");
        }
        Estimate estimate = new Estimate(jobId, note, parts, laborServices);
        return estimateRepository.save(estimate);
    }

    private void validateTransition(RepairJob job, JobStatus newStatus) {
        JobStatus current = job.getStatus();

        if (!current.canTransitionTo(newStatus)) {
            if (newStatus == JobStatus.CLOSED) {
                throw new InvalidStatusTransitionException(
                        "Only orders in READY status can be closed");
            }
            throw new InvalidStatusTransitionException(
                    "Cannot change status from " + current.name() + " to " + newStatus.name());
        }

        Optional<Estimate> latest = estimateRepository.findLatestByJobId(job.getId());

        if (newStatus == JobStatus.REPAIRING) {
            boolean approved = latest.map(Estimate::isApproved).orElse(false);
            if (!approved) {
                throw new InvalidStatusTransitionException(
                        "No estimate has been approved by the customer yet");
            }
            if (current == JobStatus.WAITING_FOR_APPROVAL) {
                throw new InvalidStatusTransitionException(
                        "Process estimate approval in the Estimate tab.");
            }
        }

        if (newStatus == JobStatus.WAITING_FOR_APPROVAL
                && !latest.map(Estimate::isDraft).orElse(false)) {
            throw new InvalidStatusTransitionException(
                    "Save a draft estimate before sending the repair for customer approval.");
        }

        if (newStatus == JobStatus.CLOSED && current == JobStatus.WAITING_FOR_APPROVAL) {
            boolean rejected = latest.map(Estimate::isRejected).orElse(false);
            if (!rejected) {
                throw new InvalidStatusTransitionException(
                        "Order can only be closed from WAITING_FOR_APPROVAL after the customer rejects the estimate");
            }
            throw new InvalidStatusTransitionException(
                    "Process estimate rejection in the Estimate tab.");
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

        repairJobRepository.updateWithHistory(job, history);
    }

    @Override
    public RepairJob createJob(int deviceId, String issueDescription, int desiredDays) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find device with id " + deviceId));

        RepairJob job = new RepairJob(device, issueDescription, desiredDays);

        return repairJobRepository.saveWithInitialHistory(job, null);
    }

    @Override
    public void approveEstimate(int estimateId) {
        Estimate estimate = findEstimateOrThrow(estimateId);
        RepairJob job = findJobOrThrow(estimate.getJobId());

        checkIsLatestEstimate(estimate);

        if (!estimate.isDraft()) {
            throw new InvalidStatusTransitionException("Only estimates with the DRAFT status can be approved");
        }

        JobStatus oldStatus = job.getStatus();

        if (!oldStatus.canTransitionTo(JobStatus.REPAIRING)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status from " + oldStatus.name() + " to " + JobStatus.REPAIRING.name());
        }

        job.setStatus(JobStatus.REPAIRING);
        StatusHistory history = new StatusHistory(job.getId(), oldStatus, JobStatus.REPAIRING,
                "Customer approves the estimate");

        estimateRepository.updateEstimateStatusAndJob(estimateId, EstimateStatus.APPROVED, job, history);
    }

    @Override
    public void rejectEstimate(int estimateId, String note) {
        Estimate estimate = findEstimateOrThrow(estimateId);
        RepairJob job = findJobOrThrow(estimate.getJobId());

        checkIsLatestEstimate(estimate);

        if (!estimate.isDraft()) {
            throw new InvalidStatusTransitionException("Only estimates with the DRAFT status can be rejected");
        }

        JobStatus oldStatus = job.getStatus();

        if (!oldStatus.canTransitionTo(JobStatus.CLOSED) || oldStatus != JobStatus.WAITING_FOR_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "Only orders with the WAITING_FOR_APPROVAL status can have their estimates rejected");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());

        String historyNote = (note == null || note.isBlank()) ? "Customer rejects the estimate" : note;
        StatusHistory history = new StatusHistory(job.getId(), oldStatus, JobStatus.CLOSED, historyNote);

        estimateRepository.updateEstimateStatusAndJob(estimateId, EstimateStatus.REJECTED, job, history);
    }

    private void checkIsLatestEstimate(Estimate estimate) {
        Optional<Estimate> latest = estimateRepository.findLatestByJobId(estimate.getJobId());
        if (latest.isEmpty() || latest.get().getId() != estimate.getId()) {
            throw new InvalidStatusTransitionException("Only the latest estimate for an order can be processed");
        }
    }
}
