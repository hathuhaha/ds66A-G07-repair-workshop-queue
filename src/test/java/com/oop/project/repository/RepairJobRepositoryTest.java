package com.oop.project.repository;

import java.sql.Connection;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oop.project.exception.DataAccessException;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.model.StatusHistory;
import com.oop.project.repository.impl.RepairJobRepositoryImpl;
import com.oop.project.support.TestDatabase;

class RepairJobRepositoryTest {

    private RepairJobRepository repository;

    @BeforeEach
    void setUp() {
        TestDatabase.reset();
        repository = new RepairJobRepositoryImpl(TestDatabase.dataSource());
    }

    private static List<Integer> ids(List<RepairJob> jobs) {
        return jobs.stream().map(RepairJob::getId).collect(Collectors.toList());
    }

    @Test
    void findQueueSortsByDeadlineThenCreatedAtThenId() {
        assertEquals(List.of(2, 1, 4, 5, 3, 7), ids(repository.findQueue()));
    }

    @Test
    void findQueueByStatusKeepsTheSameOrderAndSkipsClosed() {
        List<RepairJob> jobs = repository.findQueue(EnumSet.of(
                JobStatus.RECEIVED, JobStatus.DIAGNOSING, JobStatus.REPAIRING, JobStatus.CLOSED));
        assertEquals(List.of(1, 5, 3), ids(jobs));
    }

    @Test
    void findByIdLoadsDeviceAndCustomer() {
        RepairJob job = repository.findById(1).orElseThrow();
        assertNotNull(job.getDevice());
        assertNotNull(job.getDevice().getCustomer());
    }

    @Test
    void saveWithInitialHistorySetsIdAndTimestamps() {
        RepairJob job = repository.findById(1).orElseThrow();
        RepairJob copy = new RepairJob(job.getDevice(), "New problem", 4);
        RepairJob saved = repository.saveWithInitialHistory(copy, "Job received");
        assertEquals(true, saved.getId() > 0);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getDeadline()); // would throw DomainException before the L13 fix
    }

    @Test
    void updateWithHistoryRollsBackWhenTheHistoryInsertFails() throws Exception {
        // Force every INSERT into status_history to fail
        try (Connection c = TestDatabase.dataSource().getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE OR REPLACE FUNCTION fail_history() RETURNS trigger AS $$ "
                    + "BEGIN RAISE EXCEPTION 'forced failure'; END; $$ LANGUAGE plpgsql");
            st.execute("CREATE TRIGGER trg_fail_history BEFORE INSERT ON status_history "
                    + "FOR EACH ROW EXECUTE FUNCTION fail_history()");
        }
        RepairJob job = repository.findById(3).orElseThrow(); // RECEIVED
        job.setStatus(JobStatus.DIAGNOSING);
        StatusHistory history = new StatusHistory(3, JobStatus.RECEIVED, JobStatus.DIAGNOSING, "start");

        assertThrows(DataAccessException.class, () -> repository.updateWithHistory(job, history));

        assertEquals(JobStatus.RECEIVED, repository.findById(3).orElseThrow().getStatus());
    }
}