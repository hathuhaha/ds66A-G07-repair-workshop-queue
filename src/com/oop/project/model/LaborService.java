package com.oop.project.model;

import java.math.BigDecimal;

import com.oop.project.exception.DomainException;

public class LaborService implements EstimateItem {
    private int id;
    private int estimateId;
    private String description;
    private BigDecimal hours;
    private BigDecimal hourlyRate;

    public LaborService(int id, int estimateId, String description, BigDecimal hours, BigDecimal hourlyRate) {
        this.id = id;
        this.estimateId = estimateId;
        this.description = validateDescription(description);
        this.hours = validateHours(hours);
        this.hourlyRate = validateHourlyRate(hourlyRate);
    }

    public LaborService(int estimateId, String description, BigDecimal hours, BigDecimal hourlyRate) {
        this(0, estimateId, description, hours, hourlyRate);
    }

    private static String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DomainException("Description must not be empty");
        }
        return description.trim();
    }

    private static BigDecimal validateHours(BigDecimal hours) {
        if (hours == null) {
            throw new DomainException("Hours must not be empty");
        }
        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Hours must be greater than 0");
        }

        return hours;
    }

    private static BigDecimal validateHourlyRate(BigDecimal hourlyRate) {
        if (hourlyRate == null) {
            throw new DomainException("Hourly Rate must not be empty");
        }
        if (hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Hourly Rate must be greater than 0");
        }
        return hourlyRate;
    }

    public int getId() {
        return id;
    }

    public int getEstimateId() {
        return estimateId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public String getName() {
        return description;
    }

    @Override
    public BigDecimal getCost() {
        return hours.multiply(hourlyRate);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEstimateId(int estimateid) {
        this.estimateId = estimateid;
    }
}
