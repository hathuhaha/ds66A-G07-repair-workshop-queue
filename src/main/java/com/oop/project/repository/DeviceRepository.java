package com.oop.project.repository;

import com.oop.project.model.Device;

import java.util.List;

public interface DeviceRepository extends GenericRepository<Device, Integer> {

    List<Device> findByCustomerId(int customerId);
}