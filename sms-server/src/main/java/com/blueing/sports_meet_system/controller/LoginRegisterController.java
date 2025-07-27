package com.blueing.sports_meet_system.controller;


import com.blueing.sports_meet_system.exception.businessEception.AlreadyJoined;
import com.blueing.sports_meet_system.exception.businessEception.InviteCodeException;
import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.service.LoginRegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class LoginRegisterController {

    @Autowired
    LoginRegisterService loginRegisterService;

    @PostMapping("/login")
    public Result<Object> login(String username, String password) throws UserNamePasswordNoExistException {
        log.info("{}试图登录", username);
        List<Object> data = loginRegisterService.login(username, password);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Object> register(@ModelAttribute User user, String inviteCode, String powerDegree) throws Exception {
        log.info("{}试图注册", user.getName());
        loginRegisterService.register(user, inviteCode, powerDegree);
        return Result.success("注册成功！");
    }

}
