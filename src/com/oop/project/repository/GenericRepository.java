package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

/**
 * Các thao tác CRUD chung (contract mục 7).
 *
 * Quy ước:
 * - findById trả Optional.empty() khi không có, không trả null.
 * - findAll trả danh sách rỗng khi không có, không trả null.
 * - save trả về entity đã có id (và created_at, updated_at) do DB sinh.
 * - update và delete ném EntityNotFoundException nếu id không tồn tại.
 */
public interface GenericRepository<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    void update(T entity);

    void delete(ID id);
}