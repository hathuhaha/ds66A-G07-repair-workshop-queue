package com.oop.project;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.oop.project.config.AppContext;
import com.oop.project.config.DatabaseConnection;
import com.oop.project.model.Estimate;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;

/**
 * Temporary manual check against the real repair_shop database.
 * Run: mvn compile exec:java -Dexec.mainClass=com.oop.project.DbSmoke
 * It writes data (one job, one estimate), so re-run schema.sql + seed_data.sql
 * afterwards.
 * Delete this class once the automated tests cover the same ground.
 */
public class DbSmoke {

    public static void main(String[] args) {
        System.out.println("=== DbSmoke STARTED ===");
        try {
            AppContext ctx = AppContext.create();

            // 1. Read + ORDER BY deadline. Expected with the current seed: [2, 1, 4, 5, 3, 7]
            System.out.println("queue ids      = " + ids(ctx.repairJobRepository().findQueue()));
            // Expected: [1, 5, 3]
            System.out.println("technician ids = " + ids(ctx.repairJobRepository()
                    .findQueue(EnumSet.of(JobStatus.RECEIVED, JobStatus.DIAGNOSING, JobStatus.REPAIRING))));

            // 2. JOIN read
            RepairJob job1 = ctx.repairJobRepository().findById(1).orElseThrow();
            System.out.println("job 1          = " + job1.getStatus() + ", device " + job1.getDevice().getDeviceType()
                    + ", customer " + job1.getDevice().getCustomer().getFullName());

            // 3. ILIKE ... ESCAPE: '%' must be matched literally, so no customer is found
            System.out.println("search '%'     = " + ctx.customerRepository().searchByName("%").size() + " (expected 0)");
            System.out.println("search ''      = " + ctx.customerRepository().searchByName("").size() + " (all customers)");

            // 4. Estimate lines are loaded (L11). Estimate 2 has 2 parts and 1 labor service
            Estimate e2 = ctx.estimateRepository().findById(2).orElseThrow();
            System.out.println("estimate 2     = " + e2.getParts().size() + " parts, "
                    + e2.getLaborServices().size() + " labor, total " + e2.getTotalCost());

            // 5. INSERT ... RETURNING + createdAt (L13)
            RepairJob created = ctx.jobService().createJob(1, "Smoke test job", 3);
            System.out.println("new job        = id " + created.getId() + ", deadline " + created.getDeadline());

            // 6. FOR UPDATE + version assignment (L12). Job 2 already has version 1, so expect 2
            Estimate saved = ctx.estimateRepository().save(new Estimate(2, "Smoke test estimate", null, null));
            System.out.println("new estimate   = id " + saved.getId() + ", version " + saved.getVersion() + " (expected 2)");

            System.out.println("OK. Now re-run schema.sql and seed_data.sql to remove the test rows.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close();
        }
    }

    // Phương thức phụ trợ để chuyển List<RepairJob> thành List<Integer> chứa các ID
    private static List<Integer> ids(List<RepairJob> jobs) {
        List<Integer> ids = new ArrayList<>();
        for (RepairJob job : jobs) {
            ids.add(job.getId());
        }
        return ids;
    }
}
