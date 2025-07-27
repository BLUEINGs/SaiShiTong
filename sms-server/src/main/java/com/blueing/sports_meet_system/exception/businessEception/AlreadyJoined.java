package com.blueing.sports_meet_system.exception.businessEception;

public class AlreadyJoined extends RuntimeException {
    public AlreadyJoined() {
        super("用户已加入该团体");
    }
}
