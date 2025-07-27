package com.blueing.sports_meet_system.exception.businessEception;

public class InviteCodeDuplicateException extends BusinessException {

    public InviteCodeDuplicateException(String message) {
        super("邀请码重复："+message);
    }

}
