package com.oop.project.model;

import com.oop.project.exception.DomainException;

public class LaborService implements EstimateItem {
    private int id;
    private String description;
    private double hours;
    private double hourlyRate;

    private static String validateDescription(String description) throws DomainException {
        if (description == null || description.isBlank()) {
            throw new DomainException("Description must not be empty");
        }

        return description.trim();
    }

    private static double validateHours(double hours) throws DomainException {
        if (hours <= 0) {
            throw new DomainException("Hours must be greater than 0");
        }

        return hours;
    }

    private static double validateHourlyRate(double hourlyRate) throws DomainException {
        if (hourlyRate < 0) {
            throw new DomainException("Hourly Rate must not be negative");
        }

        return hourlyRate;
    }

    public LaborService(int id, String description, double hours, double hourlyRate) throws DomainException {
        this.id = id;
        this.description = validateDescription(description);
        this.hours = validateHours(hours);
        this.hourlyRate = validateHourlyRate(hourlyRate);
    }

    public LaborService(String description, double hours, double hourlyRate) throws DomainException {
        this(0, description, hours, hourlyRate);
    }

    @Override
    public String getName() {
        return description;
    }

    @Override
    public double getCost() {
        return hours * hourlyRate;
    }

    public int getId() {
        return id;
    }

    public double getHours() {
        return hours;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }
}
