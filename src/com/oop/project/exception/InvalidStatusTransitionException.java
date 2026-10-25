package com.oop.project.exception;

public class InvalidStatusTransitionException extends DomainException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
