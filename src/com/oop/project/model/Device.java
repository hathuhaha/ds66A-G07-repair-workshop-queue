package com.oop.project.model;

import java.time.LocalDateTime;

import com.oop.project.exception.DomainException;

public class Device {
    private int id;
    private Customer customer;
    private String deviceType;
    private String brand;
    private String model;
    private String serialNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static Customer validateCustomer(Customer customer) {
        if (customer == null) {
            throw new DomainException("Device must belong to a customer");
        }

        return customer;
    }

    private static String validateDeviceType(String deviceType) {
        if (deviceType == null || deviceType.isBlank()) {
            throw new DomainException("Device Type must not be empty");
        }
        return deviceType.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public Device(int id, Customer customer, String deviceType, String brand, String model, String serialNumber,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customer = validateCustomer(customer);
        this.deviceType = validateDeviceType(deviceType);
        this.brand = normalizeOptional(brand);
        this.model = normalizeOptional(model);
        this.serialNumber = normalizeOptional(serialNumber);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Device(Customer customer, String deviceType, String brand, String model, String serialNumber) {
        this(0, customer, deviceType, brand, model, serialNumber, null, null);
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(int id) {
        this.id = id;
    }
}
