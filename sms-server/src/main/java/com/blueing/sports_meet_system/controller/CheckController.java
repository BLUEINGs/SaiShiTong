package com.blueing.sports_meet_system.controller;


import com.blueing.sports_meet_system.pojo.Check;
import com.blueing.sports_meet_system.pojo.Result;

import com.blueing.sports_meet_system.service.imp.CheckServiceA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class CheckController {

    @Autowired
    private CheckServiceA checkServiceA;

    @GetMapping("/chcke/noticeG")
     public Result<Object> list(Integer spId){
        log.info("查询检录数据");
        List<Check> checks = checkServiceA.listCheck(spId);
        return Result.success(checks);
    }
}
