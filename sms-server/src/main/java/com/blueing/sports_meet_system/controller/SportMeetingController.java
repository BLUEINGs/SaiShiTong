package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.SportMeetingsMapper;
import com.blueing.sports_meet_system.pojo.NewMeeting;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.SportMeeting;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.service.imp.SportMeetingServiceA;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class SportMeetingController {

    @Autowired
    private SportMeetingsMapper sportMeetingsMapper;

    @Autowired
    private SportMeetingServiceA sportMeetingServiceA;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/createMeeting")
    public Result<Object> createSportMeeting(@ModelAttribute NewMeeting sportMeeting,MultipartFile cover) throws IOException, UserNamePasswordNoExistException {
        log.info("负责人创建了运动会");
        List<Object> newToken = sportMeetingServiceA.addSportMeeting(sportMeeting, cover);
        return Result.success(newToken);
    }

    @PostMapping("/joinSportMeeting")
    public Result<Object> joinSportMeeting(Integer smId,String inviteCode) throws UserNamePasswordNoExistException {
        log.info("用户加入了运动会");
        log.info("code值为：{}",inviteCode);
        sportMeetingServiceA.joinSportMeeting(smId,inviteCode);
        return Result.success(null);
    }

    @GetMapping("/getMeetingInfo")
    public Result<Object> getMeetingInfo(){
        User currentUser = Interceptor.getCurrentUser();
        log.info("运动会操办人查询了自己的运动会数据");
        return Result.success(sportMeetingsMapper.getSportMeetingBySmId(currentUser.getSmId()));
    }

    @PutMapping({"/updateMeetingBasic"})
    public Result<Object> updateMeetingInfo(String name, MultipartFile cover) throws IOException {
        log.info("运动员操办人更新了比赛基本信息");
        SportMeeting sportMeeting = new SportMeeting();
        sportMeeting.setName(name);
        sportMeetingServiceA.updateMeetingInfo(sportMeeting,cover);
        return Result.success(null);
    }

    @PutMapping({"/updateMeetingTime"})
    public Result<Object> updateMeetingTime(@RequestBody SportMeeting sportMeeting) throws IOException {
        log.info("运动员操办人更新了比赛时间");
        sportMeetingServiceA.updateMeetingInfo(sportMeeting,null);
        return Result.success(null);
    }

    @GetMapping("/meetingTemplates")
    public Result<List<SportMeeting>> getMeeting(){
        log.info("用户查询了运动会模板");
        return Result.success(sportMeetingServiceA.getDemoMeetings());
    }

    @DeleteMapping("/deleteMeeting")
    public Result<Object> dropMeeting(){
        log.info("用户彻底删除了运动会");
        sportMeetingServiceA.deleteMeeting();
        return Result.success(null);
    }

}
