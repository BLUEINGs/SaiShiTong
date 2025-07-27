package com.blueing.sports_meet_system.exception.businessEception;

public class NotAllScoredException extends RuntimeException {

    public NotAllScoredException(String message) {
        super(message);
    }

    public NotAllScoredException(){
        super("按当前排名赋分模式，赋分仅在完全录入成绩后");
    }

}
