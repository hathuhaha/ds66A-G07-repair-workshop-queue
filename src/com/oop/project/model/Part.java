package com.oop.project.model;

import com.oop.project.exception.DomainException;

public class Part implements EstimateItem {
    private int id;
    private String name;
    private double unitPrice;
    private int quantity;

    private static String validateName(String name) throws DomainException {
        if (name == null || name.isBlank()) {
            throw new DomainException("Part Name must not be empty");
        }

        return name.trim();
    }

    private static double validateUnitPrice(double unitPrice) throws DomainException {
        if (unitPrice < 0) {
            throw new DomainException("Unit Price must not be negative");
        }

        return unitPrice;
    }

    private static int validateQuantity(int quantity) throws DomainException {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than 0");
        }

        return quantity;
    }

    public Part(int id, String name, double unitPrice, int quantity) throws DomainException {
        this.id = id;
        this.name = validateName(name);
        this.unitPrice = validateUnitPrice(unitPrice);
        this.quantity = validateQuantity(quantity);
    }

    public Part(String name, double unitPrice, int quantity) throws DomainException {
        this(0, name, unitPrice, quantity);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getCost() {
        return quantity * unitPrice;
    }

    public int getId() {
        return id;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
