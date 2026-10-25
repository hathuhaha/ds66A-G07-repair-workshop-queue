package com.oop.project.exception;

/**
 * Chuyển trạng thái đơn sửa chữa không hợp lệ (contract mục 6 và 9).
 * Thông báo viết bằng tiếng Việt để giao diện hiển thị thẳng cho người dùng,
 * ví dụ: "Chỉ đơn ở trạng thái READY mới được đóng".
 */
public class InvalidStatusTransitionException extends DomainException {

    private static final long serialVersionUID = 1L;

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}