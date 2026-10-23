package com.oop.project.model;

import com.oop.project.exception.DomainException;

public class Customer {
    private Integer id;
    private String name;
    private String phone;
    private String email;
    private String address;

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
            throw new DomainException("Phone Number must contain between 9 and 11 digits");
        }

        return phone;
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Customer(Integer id, String name, String phone, String email, String address) throws DomainException {
        this.id = id;
        this.name = validateName(name);
        this.phone = validatePhone(phone);
        this.email = normalizeOptional(email);
        this.address = normalizeOptional(address);
    }

    public Customer(String name, String phone, String email, String address) throws DomainException {
        this(null, name, phone, email, address);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
