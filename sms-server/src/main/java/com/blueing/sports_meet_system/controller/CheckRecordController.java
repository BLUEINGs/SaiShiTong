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
        log.info("查询检录数据");
        User currentUser = Interceptor.getCurrentUser();
//        log.info(currentUser.toString());

        CheckRecord checkRecords = checkRecordServiceA.queryCheckRecord(currentUser.getSmId(), spId,eventType);
        return Result.success(checkRecords);
//        return Result.error(checks,"异常信息");
    }

    @PutMapping("/saishitong/check/state")
    public Result<Object> modifyCheckStatus(Integer spId,Integer pid,Integer checkState){
        log.info("修改检录状态");
        User currentUser = Interceptor.getCurrentUser();
        checkRecordServiceA.modifyCheckStatus(currentUser.getSmId(),spId,pid,checkState);
        return Result.success(null);
    }

    @PutMapping("/saishitong/check/drawing")
    public Result<Object> addTrack(Integer spId,Integer gid){
        log.info("添加田径赛道");
        User currentUser = Interceptor.getCurrentUser();
        checkRecordServiceA.addTrack(currentUser.getSmId(),spId,gid);
        return Result.success(null);
    }

}
