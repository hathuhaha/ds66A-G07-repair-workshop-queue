package com.oop.project.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oop.project.config.AppContext;
import com.oop.project.model.Customer;
import com.oop.project.model.Device;
import com.oop.project.support.TestDatabase;

public class DeviceRepositoryTest {

    private DeviceRepository deviceRepository;
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Reset database và nạp lại dữ liệu mẫu trước mỗi test case
        TestDatabase.reset();

        AppContext ctx = AppContext.create();
        deviceRepository = ctx.deviceRepository();
        customerRepository = ctx.customerRepository();
    }

    @Test
    void testSaveAndFindById() {
        // Thiết bị bắt buộc phải thuộc về một Customer
        Customer customer = new Customer("Test Owner", "0912345678", "owner@example.com", "Hanoi");
        Customer savedCustomer = customerRepository.save(customer);

        // Tạo thiết bị mới gắn với customer vừa lưu
        Device newDevice = new Device(savedCustomer, "Laptop", "Dell", "XPS 15", "SN123456");
        Device savedDevice = deviceRepository.save(newDevice);

        assertNotNull(savedDevice.getId(), "ID thiết bị sau khi lưu không được null");

        // Tìm kiếm theo ID
        Optional<Device> found = deviceRepository.findById(savedDevice.getId());
        assertTrue(found.isPresent(), "Phải tìm thấy thiết bị trong database");
        assertEquals("XPS 15", found.get().getModel());
        assertEquals("SN123456", found.get().getSerialNumber());
        assertNotNull(found.get().getCustomer(), "Thiết bị phải chứa thông tin khách hàng");
        assertEquals(savedCustomer.getId(), found.get().getCustomer().getId());
    }

    @Test
    void testUpdate() {
        Customer customer = new Customer("Test Owner 2", "0987654321", "owner2@example.com", "Danang");
        Customer savedCustomer = customerRepository.save(customer);

        Device newDevice = new Device(savedCustomer, "Phone", "Apple", "iPhone 13", "IP13000");
        Device savedDevice = deviceRepository.save(newDevice);

        // Cập nhật thông tin thiết bị bằng cách truyền đối tượng mang ID cũ và dữ liệu
        // mới
        Device updatedDevice = new Device(
                savedDevice.getId(),
                savedCustomer,
                "Phone",
                "Apple",
                "iPhone 13 Pro",
                "IP13000",
                null,
                null);
        deviceRepository.update(updatedDevice); // hoặc update tùy theo method của repo

        // Kiểm tra sau khi update
        Optional<Device> found = deviceRepository.findById(savedDevice.getId());
        assertTrue(found.isPresent());
        assertEquals("iPhone 13 Pro", found.get().getModel());
    }

    @Test
    void testDelete() {
        Customer customer = new Customer("Test Owner 3", "0900000000", "owner3@example.com", "Hue");
        Customer savedCustomer = customerRepository.save(customer);

        Device newDevice = new Device(savedCustomer, "Tablet", "Apple", "iPad Air", "IPAD999");
        Device savedDevice = deviceRepository.save(newDevice);

        assertNotNull(savedDevice.getId());

        // Xóa thiết bị
        deviceRepository.delete(savedDevice.getId());

        // Kiểm tra xem đã bị xóa chưa
        Optional<Device> found = deviceRepository.findById(savedDevice.getId());
        assertTrue(found.isEmpty(), "Thiết bị sau khi xóa phải không còn tồn tại");
    }
}