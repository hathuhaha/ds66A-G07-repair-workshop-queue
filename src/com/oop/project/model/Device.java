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

    private static String validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainException(fieldName + " must not be empty");
        }

        return value.trim();
    }

    public Device(int id, Customer customer, String deviceType, String brand, String model, String serialNumber,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customer = customer;
        this.deviceType = validateText(deviceType, "Device Type");
        this.brand = validateText(brand, "Device Brand");
        this.model = validateText(model, "Device Model");
        this.serialNumber = validateText(serialNumber, "Device Serial Number");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Device(String serialNumber, String deviceType, String model) {
        this(0, serialNumber, deviceType, model);
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
