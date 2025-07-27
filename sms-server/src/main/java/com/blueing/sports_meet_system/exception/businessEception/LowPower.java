package com.blueing.sports_meet_system.exception.businessEception;

public class LowPower extends RuntimeException {
    public LowPower() {
        super("权限不足！");
    }
}
