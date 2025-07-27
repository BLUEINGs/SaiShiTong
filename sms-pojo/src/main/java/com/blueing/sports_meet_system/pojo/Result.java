package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<E> {
    private int code;
    private E data;
    private String message;

    public static <E> Result<E> success(E data) {
        return new Result<>(1, data, "SUCCESS");
    }

    public static <E> Result<E> warning(E data,String message){
        return new Result<>(1,data,message);
    }

    public static <E> Result<E> error(E data,String message){
        return new Result<>(0,data,message);
    }

}
