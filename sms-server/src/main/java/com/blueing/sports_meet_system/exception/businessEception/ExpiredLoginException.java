package com.blueing.sports_meet_system.exception.businessEception;

public class ExpiredLoginException extends RuntimeException {
    public ExpiredLoginException(String message) {
        super(message);
    }
}
