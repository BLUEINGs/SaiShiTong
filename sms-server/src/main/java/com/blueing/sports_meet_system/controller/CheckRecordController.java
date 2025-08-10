package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.pojo.CheckRecord;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.service.imp.CheckRecordRecordServiceA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin
@RestController
public class CheckRecordController {

    @Autowired
    private CheckRecordRecordServiceA checkRecordServiceA;

    @GetMapping("/saishitong/check/noticeG")
     public Result<Object> queryCheckRecord(Integer spId,Integer eventType){
        /*获取当前User*/
        User currentUser = Interceptor.getCurrentUser();
//        log.info(currentUser.toString());
        log.info("查询检录数据");
        CheckRecord checkRecords = checkRecordServiceA.queryCheckRecord(currentUser.getSmId(), spId,eventType);
        return Result.success(checkRecords);
//        return Result.error(checks,"异常信息");
    }

    @PutMapping("/saishitong/check/state")
    public Result<Object> modifyCheckStatus(Integer spId,Integer pid,Integer state){
        User currentUser = Interceptor.getCurrentUser();
        checkRecordServiceA.modifyCheckStatus(currentUser.getSmId(),spId,pid,state);
        return Result.success(null);
    }

}
