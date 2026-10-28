package com.oop.project.exception;

/**
 * Lỗi truy cập dữ liệu (bọc SQLException).
 * Là unchecked exception để các tầng trên không phải import java.sql.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}