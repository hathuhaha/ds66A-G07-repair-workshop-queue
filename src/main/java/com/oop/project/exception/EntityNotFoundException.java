package com.oop.project.exception;

/**
 * Không tìm thấy dữ liệu cần thiết (ví dụ cập nhật hoặc xóa một id không tồn
 * tại). Là lỗi nghiệp vụ nên kế thừa DomainException để giao diện bắt chung.
 */
public class EntityNotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}