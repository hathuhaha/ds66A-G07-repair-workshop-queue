package com.oop.project.exception;

/**
 * Không tìm thấy dữ liệu cần thiết (ví dụ cập nhật hoặc xóa một id không tồn
 * tại).
 */
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}