package com.oop.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.oop.project.exception.DomainException;
import com.oop.project.exception.EntityNotFoundException;
import com.oop.project.exception.InvalidStatusTransitionException;
import com.oop.project.model.Device;
import com.oop.project.model.Estimate;
import com.oop.project.model.EstimateStatus;
import com.oop.project.model.JobStatus;
import com.oop.project.model.LaborService;
import com.oop.project.model.Part;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.repository.DeviceRepository;
import com.oop.project.repository.EstimateRepository;
import com.oop.project.repository.RepairJobRepository;

/**
 * Checks the business rules (contract section 6), then calls the repository
 * method that runs the whole change in one transaction (contract section 8).
 */
public class JobServiceImpl implements JobService {

    private final RepairJobRepository jobRepository;
    private final DeviceRepository deviceRepository;
    private final EstimateRepository estimateRepository;

    public JobServiceImpl(RepairJobRepository jobRepository, DeviceRepository deviceRepository,
            EstimateRepository estimateRepository) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.deviceRepository = Objects.requireNonNull(deviceRepository, "deviceRepository");
        this.estimateRepository = Objects.requireNonNull(estimateRepository, "estimateRepository");
    }

    @Override
    public RepairJob createJob(int deviceId, String issueDescription, int desiredDays) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id = " + deviceId));
        RepairJob job = new RepairJob(device, issueDescription, desiredDays);
        return jobRepository.saveWithInitialHistory(job, "Job received");
    }

    @Override
    public void updateJobStatusWithHistory(int jobId, JobStatus newStatus, String note) {
        Objects.requireNonNull(newStatus, "newStatus");
        RepairJob job = requireJob(jobId);
        JobStatus oldStatus = job.getStatus();
        requireTransition(oldStatus, newStatus);

        // Leaving WAITING_FOR_APPROVAL depends on the latest estimate (contract section
        // 6, L6)
        if (oldStatus == JobStatus.WAITING_FOR_APPROVAL) {
            EstimateStatus needed = newStatus == JobStatus.REPAIRING
                    ? EstimateStatus.APPROVED
                    : EstimateStatus.REJECTED;
            Estimate latest = estimateRepository.findLatestByJobId(jobId)
                    .orElseThrow(() -> new InvalidStatusTransitionException("The job has no estimate yet"));
            if (latest.getStatus() != needed) {
                throw new InvalidStatusTransitionException("Cannot move the job to " + newStatus
                        + ": the latest estimate must be " + needed + " but it is " + latest.getStatus());
            }
        }

        changeStatus(job, newStatus);
        jobRepository.updateWithHistory(job, new StatusHistory(jobId, oldStatus, newStatus, note));
    }

    @Override
    public Estimate createEstimate(int jobId, List<Part> parts, List<LaborService> laborServices, String note) {
        RepairJob job = requireJob(jobId);
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new DomainException("Cannot create an estimate for a closed job");
        }
        // Version is assigned by the repository, never by the service
        return estimateRepository.save(new Estimate(jobId, note, parts, laborServices));
    }

    @Override
    public void approveEstimate(int estimateId) {
        decideEstimate(estimateId, EstimateStatus.APPROVED, JobStatus.REPAIRING, "Estimate approved");
    }

    @Override
    public void rejectEstimate(int estimateId, String note) {
        String text = (note == null || note.isBlank()) ? "Customer rejected the estimate" : note;
        decideEstimate(estimateId, EstimateStatus.REJECTED, JobStatus.CLOSED, text);
    }

    @Override
    public void updateEstimatedReadyAt(int jobId, LocalDateTime estimatedReadyAt) {
        RepairJob job = requireJob(jobId);
        job.setEstimatedReadyAt(estimatedReadyAt);
        jobRepository.update(job);
    }

    // ------------------------------------------------------------------ helpers

    private void decideEstimate(int estimateId, EstimateStatus decision, JobStatus nextJobStatus, String note) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new EntityNotFoundException("Estimate not found with id = " + estimateId));
        RepairJob job = requireJob(estimate.getJobId());

        if (!estimate.isDraft()) {
            throw new InvalidStatusTransitionException(
                    "Estimate " + estimateId + " is already " + estimate.getStatus());
        }
        boolean isLatest = estimateRepository.findLatestByJobId(job.getId())
                .map(latest -> latest.getId() == estimateId)
                .orElse(false);
        if (!isLatest) {
            throw new InvalidStatusTransitionException("Only the latest estimate of a job can be decided");
        }
        if (job.getStatus() != JobStatus.WAITING_FOR_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "An estimate can only be decided while the job is WAITING_FOR_APPROVAL");
        }
        requireTransition(job.getStatus(), nextJobStatus);

        JobStatus oldStatus = job.getStatus();
        changeStatus(job, nextJobStatus);
        StatusHistory history = new StatusHistory(job.getId(), oldStatus, nextJobStatus, note);
        estimateRepository.updateEstimateStatusAndJob(estimateId, decision, job, history);
    }

    private RepairJob requireJob(int jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Repair job not found with id = " + jobId));
    }

    private static void requireTransition(JobStatus from, JobStatus to) {
        if (!from.canTransitionTo(to)) {
            throw new InvalidStatusTransitionException("Cannot change job status from " + from + " to " + to);
        }
    }

    /**
     * status and closedAt must always change together (DB CHECK
     * chk_repair_jobs_closed_at).
     */
    private static void changeStatus(RepairJob job, JobStatus next) {
        job.setStatus(next);
        job.setClosedAt(next == JobStatus.CLOSED ? LocalDateTime.now() : null);
    }
}