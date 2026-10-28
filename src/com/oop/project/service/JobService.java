package com.oop.project.service;

import java.time.LocalDateTime;
import java.util.List;

import com.oop.project.model.Estimate;
import com.oop.project.model.JobStatus;
import com.oop.project.model.LaborService;
import com.oop.project.model.Part;
import com.oop.project.model.RepairJob;

public interface JobService {
    RepairJob createJob(int deviceId, String issueDescription, int desiredDays);

    void updateJobStatusWithHistory(int jobId, JobStatus newStatus, String note);

    List<JobStatus> getAvailableNextStatuses(int jobId);

    Estimate createEstimate(int jobId, List<Part> parts, List<LaborService> laborServices, String note);

    void approveEstimate(int estimateId);

    void rejectEstimate(int estimateId, String note);

    void updateEstimatedReadyAt(int jobId, LocalDateTime estimatedReadyAt);
}
