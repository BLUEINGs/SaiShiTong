package com.blueing.sports_meet_system.exception.businessEception;

public class JwtException extends BusinessException {
    public JwtException(String message) {
        super(message);
    }

    public JwtException() {
        super("异常的JWT！轻忽擅自修改JWT信息");
    }
}
