package com.blueing.sports_meet_system.exception.mybatisException;

public class MybatisEception extends RuntimeException
{
    public MybatisEception(String message) {
        super("数据异常");
    }

}
