package com.blueing.sports_meet_system.controller;


import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.pojo.Check;
import com.blueing.sports_meet_system.pojo.Result;

import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.service.imp.CheckServiceA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin
@RestController
public class CheckController {

    @Autowired
    private CheckServiceA checkServiceA;

    @GetMapping("/saishitong/check/noticeG")
     public Result<Object> list(Integer spId){
        /*获取当前User*/
        User currentUser = Interceptor.getCurrentUser();
//        log.info(currentUser.toString());
        log.info("查询检录数据");
        Check checks = checkServiceA.listCheck(currentUser.getSmId(), spId);
        return Result.success(checks);
//        return Result.error(checks,"异常信息");
    }

}
