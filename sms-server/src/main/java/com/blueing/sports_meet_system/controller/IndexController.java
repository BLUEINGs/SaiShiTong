package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.mapper.IndexMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
public class IndexController {

    @Autowired
    private IndexService indexService;

    @Autowired
    private IndexMapper indexMapper;

    @RequestMapping("/ping")
    public String ping(){
        return "Ok";
    }

    @GetMapping("/getSportMeetings")
    public Result<List<SportMeeting>> getSportMeetings(){
        log.info("用户查询了运动会列表");
        return Result.success(indexService.getSportMeetings());
    }

    @GetMapping("/getSportMeeting/{id}")
    public Result<SportMeeting> getSportMeeting(@PathVariable Integer id){
        log.info("用户查询了smId为{}的运动会",id);
        return Result.success(indexMapper.getSportMeetingBySmId(id));
    }

    @GetMapping("/dashboard/schedule")
    public Result<Map<String,List<Sport>>> getTodaySchedule(){
        log.info("用户查询了今日日程");
        return Result.success (indexService.getTodaySchedule());
    }

    @GetMapping("/dashboard/myScores")
    public Result<List<MyScore>> getMyScores(){
        log.info("用户查询了自己的成绩");
        return Result.success(indexService.getMyScores());
    }

    @GetMapping("/dashboard/teamScore")
    public Result<List<DetailScore>> getSchoolScore(){
        log.info("用户查询了自己的团体的成绩");
        return Result.success(indexService.getSchoolScores());
    }

}
