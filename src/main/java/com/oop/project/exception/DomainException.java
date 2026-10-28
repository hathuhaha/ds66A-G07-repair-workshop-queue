package com.oop.project.exception;

/**
 * Lớp cha của các lỗi nghiệp vụ: người dùng thao tác vi phạm luật của hệ thống
 * (ví dụ đóng đơn sai điều kiện), không phải lỗi kỹ thuật.
 *
 * - Message viết bằng tiếng Việt, đủ rõ để giao diện hiển thị thẳng cho người
 * dùng.
 * - Là unchecked exception nên các tầng không phải khai báo throws.
 * - Khác với DataAccessException (lỗi kỹ thuật khi truy cập cơ sở dữ liệu):
 * giao diện có thể
 * bắt DomainException để báo cho người dùng sửa thao tác, còn
 * DataAccessException thường
 * là sự cố cần ghi log.
 *
 * Các lỗi nghiệp vụ cụ thể (ví dụ InvalidStatusTransitionException) kế thừa lớp
 * này.
 */
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}