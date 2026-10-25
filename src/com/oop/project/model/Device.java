package com.oop.project.model;

import com.oop.project.exception.DomainException;

public class Device {
    private Integer id;
    private String serialNumber;
    private String deviceType;
    private String model;
    private Integer customerId;

    private static String validateText(String value, String fieldName) throws DomainException {
        if (value == null || value.isBlank()) {
            throw new DomainException(fieldName + " must not be empty");
        }

        return value.trim();
    }

    private static Integer validateCustomerId(Integer customerId) throws DomainException {
        if (customerId == null || customerId <= 0) {
            throw new DomainException("Customer ID must be greater than 0");
        }

        return customerId;
    }

    public Device(Integer id, String serialNumber, String deviceType, String model, Integer customerId)
            throws DomainException {
        this.id = id;
        this.serialNumber = validateText(serialNumber, "Device Serial Number");
        this.deviceType = validateText(deviceType, "Device Type");
        this.model = validateText(model, "Device Model");
        this.customerId = validateCustomerId(customerId);
    }

    public Device(String serialNumber, String deviceType, String model, Integer customerId) throws DomainException {
        this(null, serialNumber, deviceType, model, customerId);
    }

    public Integer getId() {
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

    public Integer getCustomerId() {
        return customerId;
    }
}
