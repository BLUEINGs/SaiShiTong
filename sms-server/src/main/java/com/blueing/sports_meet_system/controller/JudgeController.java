package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.JudgeMapper;
import com.blueing.sports_meet_system.mapper.SportMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.imp.JudgeServiceA;
import com.blueing.sports_meet_system.service.imp.SportServiceA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class JudgeController {

    private final JudgeMapper judgeMapper;
    private final SportMapper sportMapper;
    private final SportServiceA sportServiceA;
    private final JudgeServiceA judgeServiceA;

    public JudgeController(JudgeMapper judgeMapper, SportMapper sportMapper, SportServiceA sportServiceA, JudgeServiceA judgeServiceA) {
        this.judgeMapper = judgeMapper;
        this.sportMapper = sportMapper;
        this.sportServiceA = sportServiceA;
        this.judgeServiceA = judgeServiceA;
    }

    @GetMapping("/getAvailableJudges")
    public Result<List<Judge>> getAvailableJudges(){
        log.info("用户查询可供分配的裁判");
        List<Judge> judges=judgeServiceA.getJudges();
        return Result.success(judges);
    }

    @GetMapping("/getJudgements")
    public Result<List<Judgement>> getJudgements(){
        log.info("用户查询了他所负责的比赛");
        List<Judgement> judgements = judgeServiceA.getJudgements();
        return Result.success(judgements);
    }

    @PostMapping("/assignJudges")
    public Result<Object> assignJudges(Integer spId,@RequestParam List<Integer> judgeUid){
        log.info("用户分配了裁判");
        judgeServiceA.assignJudges(spId,judgeUid);
        return Result.success(null);
    }

    @GetMapping("/getJudgementGroups")
    public Result<List<Group>> getJudgementGroups(Integer spId){
        log.info("裁判查询了他某场比赛的成员成绩");
        return Result.success(sportServiceA.getGroups(spId));
    }

    @GetMapping("/getJudgeRules")
    public Result<List<JudgeRuleB>> getJudgeRules(){
        log.info("用户查询评分规则列表");
        User currentUser = Interceptor.getCurrentUser();
        return Result.success (judgeMapper.getJudgeRulesBySmId(currentUser.getSmId()));
    }

    @PostMapping("/saveJudgeRule")
    public Result<Object> saveJudgeRule(JudgeRuleB judgeRule) {
        log.info("添加/保存了评分规则");
        judgeServiceA.saveJudgeRule(judgeRule);
        return Result.success(judgeRule.getRid());
    }

    @PostMapping("/deleteJudgeRule")
    public Result<Object> deleteJudgeRule(Integer rid){
        log.info("用户删除了rid:{}的规则",rid);
        judgeMapper.deleteJudgeRule(rid);
        return Result.success(null);
    }

    @PostMapping("/updateCompetitionRule")
    public Result<Object> updateCompetitionRule(Integer spId,Integer rid){
        User currentUser = Interceptor.getCurrentUser();
        log.info("用户更新了rid:{}的规则",rid);
        log.info("理想：{}应该为false",!currentUser.getUserType().contains(5));
//        log.info("理想：{}应该为false",!currentUser.getUserType().contains(5));
        if(!currentUser.getUserType().contains(5)&&(currentUser.getJudgeEvents()==null||!currentUser.getJudgeEvents().contains(spId))){
            return new Result<>(0,"权限不足","POWER_LOW");
        }
        judgeMapper.updateCompJudgeRuleByRid(spId,rid);
        return Result.success(null);
    }

    @PostMapping("/submitScore")
    public Result<Object> submitScore(@RequestBody Score score) throws Exception {
        log.info(score.toString());
        judgeServiceA.addPlayerScore(score);
        return Result.success(null);
    }

}
