package com.oop.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oop.project.config.AppContext;
import com.oop.project.exception.InvalidStatusTransitionException;
import com.oop.project.model.EstimateStatus;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.support.TestDatabase;

class JobServiceTest {

    private AppContext ctx;
    private JobService service;

    @BeforeEach
    void setUp() {
        TestDatabase.reset();
        ctx = new AppContext(TestDatabase.dataSource());
        service = ctx.jobService();
    }

    private RepairJob job(int id) {
        return ctx.repairJobRepository().findById(id).orElseThrow();
    }

    @Test
    void demo2ApproveEstimateMovesJobToRepairing() {
        int estimateId = ctx.estimateRepository().findLatestByJobId(2).orElseThrow().getId();
        service.approveEstimate(estimateId);

        assertEquals(EstimateStatus.APPROVED, ctx.estimateRepository().findById(estimateId).orElseThrow().getStatus());
        assertEquals(JobStatus.REPAIRING, job(2).getStatus());
    }

    @Test
    void demo2RejectEstimateClosesJobWithClosedAt() {
        int estimateId = ctx.estimateRepository().findLatestByJobId(7).orElseThrow().getId();
        service.rejectEstimate(estimateId, "Too expensive");

        assertEquals(EstimateStatus.REJECTED, ctx.estimateRepository().findById(estimateId).orElseThrow().getStatus());
        RepairJob closed = job(7);
        assertEquals(JobStatus.CLOSED, closed.getStatus());
        assertNotNull(closed.getClosedAt());
    }

    @Test
    void demo3ClosingARepairingJobIsRejected() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateJobStatusWithHistory(1, JobStatus.CLOSED, "too early"));
        assertEquals(JobStatus.REPAIRING, job(1).getStatus());
    }

    @Test
    void demo3ClosingAReadyJobSucceeds() {
        service.updateJobStatusWithHistory(4, JobStatus.CLOSED, "Customer picked up the device");
        RepairJob closed = job(4);
        assertEquals(JobStatus.CLOSED, closed.getStatus());
        assertNotNull(closed.getClosedAt());
    }

    @Test
    void anAlreadyDecidedEstimateCannotBeDecidedAgain() {
        int estimateId = ctx.estimateRepository().findLatestByJobId(2).orElseThrow().getId();
        service.approveEstimate(estimateId);
        assertThrows(InvalidStatusTransitionException.class, () -> service.rejectEstimate(estimateId, null));
    }
}