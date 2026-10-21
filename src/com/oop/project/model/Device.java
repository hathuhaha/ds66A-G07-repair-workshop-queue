package com.oop.project.model;

import com.oop.project.exception.DomainException;

public class Device {
    private int id;
    private String serialNumber;
    private String deviceType;
    private String model;
    private int customerId;

    private static String validateText(String value, String fieldName) throws DomainException {
        if (value == null || value.isBlank()) {
            throw new DomainException(fieldName + " must not be empty");
        }

        return value.trim();
    }

    private static int validateCustomerId(int customerId) throws DomainException {
        if (customerId <= 0) {
            throw new DomainException("Customer ID must be greater than 0");
        }

        return customerId;
    }

    public Device(int id, String serialNumber, String deviceType, String model, int customerId) throws DomainException {
        this.id = id;
        this.serialNumber = validateText(serialNumber, "Device Serial Number");
        this.deviceType = validateText(deviceType, "Device Type");
        this.model = validateText(model, "Device model");
        this.customerId = validateCustomerId(customerId);
    }

    public Device(String serialNumber, String deviceType, String model, int customerId) throws DomainException {
        this(0, serialNumber, deviceType, model, customerId);
    }

    public int getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getModel() {
        return model;
    }

    public int getCustomerId() {
        return customerId;
    }
}
