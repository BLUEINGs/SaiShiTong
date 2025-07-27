package com.blueing.sports_meet_system.controller;


import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.pojo.SportGroup;
import com.blueing.sports_meet_system.service.SportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@CrossOrigin
@RestController
public class SportController {

    @Autowired
    private SportService sportService;


    @GetMapping("/getSports")
    public Result<List<SportGroup>> getSport() {
        log.info("用户查询了运动信息");
        return Result.success(sportService.getAllSport());
    }

    @GetMapping("/getMatchGroups")
    public Result<List<Group>> getSportDetails(Integer spId){
        log.info("用户查询了spId为{}运动项目的分组",spId);
        return Result.success(sportService.getGroups(spId));
    }

    @PostMapping(value = "/addSport")
    public Result<Object> addSport(@RequestBody SportGroup sportGroup) {
        log.info("用户添加了赛程组");
        log.info(sportGroup.toString());
        sportService.addSportGroup(sportGroup);
        return Result.success(null);
    }


    @PostMapping("/modifySport")
    public Result<Object> modifySportGroup(@RequestBody SportGroup sportGroup) {
        log.info("用户修改了赛程组:{}",sportGroup);
        sportService.modifySportGroup(sportGroup);
        return Result.success(null);
    }

    @GetMapping("/deleteSport")
    public Result<Object> deleteSportGroup(Integer mainSpId) {
        log.info("用户删除了赛程组");
        sportService.deleteSportGroup(mainSpId);
        return Result.success(null);
    }

    @PostMapping("/signup")
    public Result<Object> signUp(Integer scId,Integer spId){
        log.info("用户报名了比赛");
        sportService.signUpSport(scId,spId);
        return Result.success(null);
    }

    @PostMapping("/cancelSignup")
    public Result<Object> cancelSignUp(Integer scId,Integer spId){
        log.info("用户取消报名比赛");
        sportService.cancelSport(scId,spId);
        return Result.success(null);
    }
}
