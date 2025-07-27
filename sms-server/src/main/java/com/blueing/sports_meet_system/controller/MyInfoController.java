package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.UserInfo;
import com.blueing.sports_meet_system.service.imp.MyInfoServiceA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class MyInfoController {

    private final MyInfoServiceA myInfoServiceA;

    public MyInfoController(MyInfoServiceA myInfoServiceA) {
        this.myInfoServiceA = myInfoServiceA;
    }

    @GetMapping("/getUserInfo")
    public Result<UserInfo> getUserInfo(){
        log.info("用户查询了自己");
        return Result.success(myInfoServiceA.handleUserInfo());
    }

    @PostMapping("/switchCurrentMeeting")
    public Result<List<Object>> switchCurrentMeeting(Integer smId) throws UserNamePasswordNoExistException {
        log.info("用户切换了要展示的运动会");
        return Result.success(myInfoServiceA.modifyUserSmId(smId));
    }

}
