package com.oop.project.config;

import javax.sql.DataSource;

import com.oop.project.repository.CustomerRepository;
import com.oop.project.repository.DeviceRepository;
import com.oop.project.repository.EstimateRepository;
import com.oop.project.repository.RepairJobRepository;
import com.oop.project.repository.StatusHistoryRepository;
import com.oop.project.repository.WarrantyRepository;
import com.oop.project.repository.impl.CustomerRepositoryImpl;
import com.oop.project.repository.impl.DeviceRepositoryImpl;
import com.oop.project.repository.impl.EstimateRepositoryImpl;
import com.oop.project.repository.impl.RepairJobRepositoryImpl;
import com.oop.project.repository.impl.StatusHistoryRepositoryImpl;
import com.oop.project.repository.impl.WarrantyRepositoryImpl;
import com.oop.project.service.JobService;
import com.oop.project.service.JobServiceImpl;

/**
 * Wires the repositories and the service from one DataSource, so UI code never
 * calls "new ...RepositoryImpl" itself. Usage: AppContext ctx =
 * AppContext.create();
 */
public final class AppContext {

    private final CustomerRepository customerRepository;
    private final DeviceRepository deviceRepository;
    private final RepairJobRepository repairJobRepository;
    private final EstimateRepository estimateRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final WarrantyRepository warrantyRepository;
    private final JobService jobService;

    public AppContext(DataSource dataSource) {
        this.customerRepository = new CustomerRepositoryImpl(dataSource);
        this.deviceRepository = new DeviceRepositoryImpl(dataSource);
        this.repairJobRepository = new RepairJobRepositoryImpl(dataSource);
        this.estimateRepository = new EstimateRepositoryImpl(dataSource);
        this.statusHistoryRepository = new StatusHistoryRepositoryImpl(dataSource);
        this.warrantyRepository = new WarrantyRepositoryImpl(dataSource);
        this.jobService = new JobServiceImpl(deviceRepository, repairJobRepository, estimateRepository);
    }

    /** Context for the real application database (.env or environment variables). */
    public static AppContext create() {
        return new AppContext(DatabaseConnection.getDataSource());
    }

    public CustomerRepository customerRepository() {
        return customerRepository;
    }

    public DeviceRepository deviceRepository() {
        return deviceRepository;
    }

    public RepairJobRepository repairJobRepository() {
        return repairJobRepository;
    }

    public EstimateRepository estimateRepository() {
        return estimateRepository;
    }

    public StatusHistoryRepository statusHistoryRepository() {
        return statusHistoryRepository;
    }

    public WarrantyRepository warrantyRepository() {
        return warrantyRepository;
    }

    public JobService jobService() {
        return jobService;
    }
}