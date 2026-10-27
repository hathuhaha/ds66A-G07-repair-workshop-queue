package com.oop.project.service;

import java.time.LocalDateTime;
import java.util.List;

import com.oop.project.model.Estimate;
import com.oop.project.model.JobStatus;
import com.oop.project.model.LaborService;
import com.oop.project.model.Part;
import com.oop.project.model.RepairJob;

/** Service contract: see docs/contract.md, section 8. */
public interface JobService {

    /** Creates the job and its first history row in one transaction. */
    RepairJob createJob(int deviceId, String issueDescription, int desiredDays);

    /**
     * Changes the job status and writes the history in one transaction.
     * Throws InvalidStatusTransitionException if the rules in section 6 are broken.
     */
    void updateJobStatusWithHistory(int jobId, JobStatus newStatus, String note);

    /** Saves a new estimate (the repository assigns its version) with its lines. */
    Estimate createEstimate(int jobId, List<Part> parts, List<LaborService> laborServices, String note);

    /** Approves the estimate and moves the job to REPAIRING in one transaction. */
    void approveEstimate(int estimateId);

    /**
     * Rejects the estimate and closes the job (closedAt is set) in one transaction.
     */
    void rejectEstimate(int estimateId, String note);

    /** Updates the estimated pick-up time (the job status does not change). */
    void updateEstimatedReadyAt(int jobId, LocalDateTime estimatedReadyAt);
}