package com.blueing.sports_meet_system.exception.businessEception;

public class UserNameDuplicateException extends RuntimeException {
  public UserNameDuplicateException() {
    super("用户名已存在");
  }
}
