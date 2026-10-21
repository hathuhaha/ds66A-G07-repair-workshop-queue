package com.oop.project.model;

import com.oop.project.exception.DomainException;

public class Customer {
    private int id;
    private String name;
    private String phone;

    private static String validateName(String name) throws DomainException {
        if (name == null || name.isBlank()) {
            throw new DomainException("Customer Name must not be empty");
        }

        name = name.trim();
        return name;
    }

    private static String validatePhone(String phone) throws DomainException {
        if (phone == null || phone.isBlank()) {
            throw new DomainException("Customer Phone must not be empty");
        }

        phone = phone.trim();

        if (!phone.matches("\\d+")) {
            throw new DomainException("Phone Number only contains numbers");
        }

        if (!phone.matches("\\d{9,11}")) {
            throw new DomainException("Phone Number must contains between 9 and 11 digits");
        }

        return phone;
    }

    public Customer(int id, String name, String phone) throws DomainException {
        this.id = id;
        this.name = validateName(name);
        this.phone = validatePhone(phone);
    }

    public Customer(String name, String phone) throws DomainException {
        this(0, name, phone);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
