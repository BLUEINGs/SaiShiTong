package com.blueing.sports_meet_system.exception.businessEception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class BusinessException extends Exception{

    public BusinessException() {
    }

    public BusinessException(String message) {
        super("业务逻辑异常："+message);
    }

}
