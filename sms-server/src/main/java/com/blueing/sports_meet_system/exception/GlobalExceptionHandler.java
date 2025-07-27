package com.blueing.sports_meet_system.exception;

import com.blueing.sports_meet_system.exception.businessEception.*;
import com.blueing.sports_meet_system.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotAllScoredException.class)
    public Result<String> nasEx(Exception e){
        e.printStackTrace();
        return new Result<>(1,e.getMessage(),"NOT_ALL_SCORED");
    }

    @ExceptionHandler(ExpiredLoginException.class)
    public Result<String> elEx(Exception e){
        e.printStackTrace();
        return new Result<>(0, e.getMessage(),"EXPIRED_LOGIN");
    }

    @ExceptionHandler(InviteCodeException.class)
    public Result<String> icEx(Exception ex){
        ex.printStackTrace();
        return new Result<>(0,ex.getMessage(),"INVALID_CODE");
    }

    @ExceptionHandler(UserNamePasswordNoExistException.class)
    public Result<String> noEx(Exception ex){
        ex.printStackTrace();
        //用户名或密码不存在
        return new Result<>(0,ex.getMessage(),"NO_EXIST_USER");
    }

    @ExceptionHandler(UserNameDuplicateException.class)
    public Result<String> usDuEx(Exception ex){
        ex.printStackTrace();
        return new Result<>(0,ex.getMessage(),"NAME_DUPLICATE");
    }

    @ExceptionHandler(NoSmIdException.class)
    public Result<String> jwtEx(Exception ex){
        return new Result<>(0, ex.getMessage(),"NO_SM_ID");
    }

//    @ExceptionHandler

    @ExceptionHandler(Exception.class)
    public Result<String> ex(Exception ex){
        ex.printStackTrace();
        return Result.error(ex.getMessage(),"");
    }

}
