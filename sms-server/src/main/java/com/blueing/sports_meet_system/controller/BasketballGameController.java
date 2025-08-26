package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.pojo.BasketballRecords;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.service.imp.BasketballGameServiceA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class BasketballGameController {
    @Autowired
    BasketballGameServiceA basketballGameServiceA;

    @PostMapping("/saishitong/basketballGame/information")
    public Result<Object> addbasketballGame(String nameA,String rgbA, String nameB,String rgbB,
                                  ZonedDateTime startTime1,ZonedDateTime endTime1,
                                  ZonedDateTime startTime2,ZonedDateTime endTime2,
                                  ZonedDateTime startTime3,ZonedDateTime endTime3,
                                  ZonedDateTime startTime4,ZonedDateTime endTime4) {

        basketballGameServiceA.addbasketballGame(nameA,rgbA,nameB,rgbB, startTime1, endTime1, startTime2, endTime2, startTime3, endTime3, startTime4, endTime4);
        return Result.success(null);
    }

    @GetMapping("/saishitong/scoringSituation")
    public Result<Object> queryTeamScoringDetailsRecord(Integer spId) {
        List<BasketballRecords> contingents= basketballGameServiceA.queryTeamScoringDetailsRecord(spId);
        return Result.success(contingents);
    }

    @GetMapping("/saishitong/scoringRecord")
    public Result<Object>  queryTeamScore(Integer spId) {
        List<BasketballRecords> teamScores = basketballGameServiceA.queryTeamScores(spId);
        return Result.success(teamScores);
    }

    @GetMapping("/saishitong/basketballEvent")
    public Result<Object> queryBasketballEvent(){
        List<BasketballRecords> BasketballEvents = basketballGameServiceA.queryBasketballEvent();
        return Result.success(BasketballEvents);
    }

    @PostMapping("/saishitong/ai/teamScores")
    public Result<Object> addAiTeamScore(Integer teId,Integer fraction){
        basketballGameServiceA.addfraction(teId,fraction);
        return Result.success(null);
    }

    @GetMapping("/saishitong/ai/basketballEvent")
    public Result<Object> queryAiBasketballEvent(){
        List<BasketballRecords> BasketballEvents = basketballGameServiceA.queryBasketballEvent();
        return Result.success(BasketballEvents);
    }

    @GetMapping("/saishitong/ai/contingent")
    public Result<Object> queryAiContingent(Integer spId){
        List<BasketballRecords> contingents = basketballGameServiceA.queryAiContingent(spId);
        return Result.success(contingents);
    }

    @GetMapping("/saishitong/ai/schedule")
    public Result<Object> queryAiSchedule(Integer spId){
        BasketballRecords basketballRecords = basketballGameServiceA.querySchedule(spId);
        return Result.success(basketballRecords);
    }

    @GetMapping("/saishitong/schedule")
    public Result<Object> queryschedule(Integer spId){
        BasketballRecords basketballRecords = basketballGameServiceA.querySchedule(spId);
        return Result.success(basketballRecords);
    }

}
