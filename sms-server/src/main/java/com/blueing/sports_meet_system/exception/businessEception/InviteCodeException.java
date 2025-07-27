package com.blueing.sports_meet_system.exception.businessEception;

public class InviteCodeException extends RuntimeException {
    public InviteCodeException() {
        super("邀请码异常");
    }

    public InviteCodeException(String message) {
        super(message);
    }
}
