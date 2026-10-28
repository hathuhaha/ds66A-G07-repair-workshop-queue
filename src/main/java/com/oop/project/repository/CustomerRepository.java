package com.oop.project.repository;

import com.oop.project.model.Customer;

import java.util.List;

public interface CustomerRepository extends GenericRepository<Customer, Integer> {

    /** Tìm khách hàng theo số điện thoại (khớp chính xác). */
    List<Customer> findByPhone(String phone);

    /** Tìm khách hàng có tên chứa từ khóa (không phân biệt hoa thường). */
    List<Customer> searchByName(String keyword);
}