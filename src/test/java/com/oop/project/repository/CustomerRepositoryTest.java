package com.oop.project.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oop.project.config.AppContext;
import com.oop.project.model.Customer;
import com.oop.project.support.TestDatabase;

public class CustomerRepositoryTest {

    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Reset database và nạp lại dữ liệu mẫu trước mỗi test case
        TestDatabase.reset();

        // Khởi tạo repository thông qua AppContext của dự án
        AppContext ctx = AppContext.create();
        customerRepository = ctx.customerRepository();
    }

    @Test
    void testSaveAndFindById() {
        // Tạo customer mới bằng constructor 4 tham số (fullName, phone, email, address)
        Customer newCustomer = new Customer("Nguyen Van A", "0912345678", "a@example.com", "Hanoi");
        Customer saved = customerRepository.save(newCustomer);

        assertNotNull(saved.getId(), "ID sau khi lưu không được null");

        // Tìm kiếm theo ID vừa lưu
        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "Phải tìm thấy khách hàng trong database");
        assertEquals("Nguyen Van A", found.get().getFullName());
        assertEquals("0912345678", found.get().getPhone());
    }

    @Test
    void testUpdate() {
        // Lưu một khách hàng ban đầu
        Customer newCustomer = new Customer("Tran Thi B", "0987654321", "b@example.com", "Danang");
        Customer saved = customerRepository.save(newCustomer);

        // Vì Customer không có setter cho tên/địa chỉ, ta cập nhật bằng cách tạo đối
        // tượng với ID cũ (hoặc dùng phương thức update của repo tùy thiết kế)
        // Dưới đây giả định hàm update hoặc update nhận object có chứa ID để thực hiện
        // câu lệnh UPDATE trong SQL
        Customer updatedCustomer = new Customer(saved.getId(), "Tran Thi B Updated", "0987654321",
                "b_updated@example.com", "Saigon", null, null);
        customerRepository.update(updatedCustomer); // hoặc customerRepository.update(...) tùy method của bạn

        // Kiểm tra kết quả sau cập nhật
        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Tran Thi B Updated", found.get().getFullName());
        assertEquals("Saigon", found.get().getAddress());
    }

    @Test
    void testDelete() {
        Customer newCustomer = new Customer("Khach Xoa", "0900000000", "xoa@example.com", "Hue");
        Customer saved = customerRepository.save(newCustomer);

        assertNotNull(saved.getId());

        // Thực hiện xóa
        customerRepository.delete(saved.getId());

        // Kiểm tra lại xem còn tồn tại không
        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertTrue(found.isEmpty(), "Khách hàng sau khi xóa phải không còn tồn tại");
    }
}