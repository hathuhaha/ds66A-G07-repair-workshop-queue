package com.oop.project.model;

import java.math.BigDecimal;

import com.oop.project.exception.DomainException;

public class Part implements EstimateItem {
    private int id;
    private int estimateId;
    private String name;
    private BigDecimal unitPrice;
    private int quantity;

    public Part(int id, int estimateId, String name, BigDecimal unitPrice, int quantity) {
        this.id = id;
        this.estimateId = estimateId;
        this.name = validateName(name);
        this.unitPrice = validateUnitPrice(unitPrice);
        this.quantity = validateQuantity(quantity);
    }

    public Part(int estimateId, String name, BigDecimal unitPrice, int quantity) {
        this(0, estimateId, name, unitPrice, quantity);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Part Name must not be empty");
        }
        return name.trim();
    }

    private static BigDecimal validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new DomainException("Unit Price must not be null");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Unit Price must not be negative");
        }
        return unitPrice;
    }

    private static int validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than 0");
        }
        return quantity;
    }

    public int getId() {
        return id;
    }

    public int getEstimateId() {
        return estimateId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEstimateId(int estimateId) {
        this.estimateId = estimateId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getCost() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
