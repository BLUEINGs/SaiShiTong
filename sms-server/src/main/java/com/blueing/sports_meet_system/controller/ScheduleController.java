package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class ScheduleController {

    @Autowired
     private ScheduleService scheduleService;

    @GetMapping("/getSchedules")
    public Result<List<Sport>> getSchedules(Integer smId){
        log.info("用户查询了日程表");
        return Result.success(scheduleService.getScheduleList(smId));
    }

    @PostMapping("/updateSchedule")
    public Result<Object> updateSchedule(Sport sport){
        log.info("用户调整了日程时间");
        scheduleService.modifySchedule(sport);
        return Result.success(null);
    }

}
