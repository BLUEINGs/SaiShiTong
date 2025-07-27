package com.blueing.sports_meet_system.exception.businessEception;

public class UserNamePasswordNoExistException extends BusinessException {

    public UserNamePasswordNoExistException(String message) {
        super("用户名或密码不存在");
    }

    public UserNamePasswordNoExistException() {
        super("用户名或密码不存在");
    }
}
