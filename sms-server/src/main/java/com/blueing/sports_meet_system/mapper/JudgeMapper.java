package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JudgeMapper {

    @Select("select u.uid,name,head from user_sm us left join users u on us.uid=u.uid where u.sm_id=#{smId} and us.sm_id=#{smId} and power_degree like '%3%' ")
    List<Judge> getJudgesBySmId(Integer smId);

    @Select("select u.uid,name,head from judges j left join users u on j.uid=u.uid where sp_id=#{spId} ")
    List<Player> getJudgesBySpId(Integer spId);

    @Select("select uid from judges j where sp_id=#{spId} ")
    List<Integer> getJudgeUidBySpId(Integer spId);

    @Select("select sp_id from judges where uid=#{uid} ")
    List<Integer> getJudgesByUid(Integer uid);

    void dropJudgeUids(List<Integer> judgeUids,Integer spId);

    void addJudges(List<Integer> judgeUids, Integer spId);

    List<Judgement> getJudgementsByUid(Integer smId,Integer uid/*, List<Integer> spIds*/);

    @Select("select sp_id,name,status,game_start_time,comp_type,venue,rid,event_type,gender from sports where sm_id=#{smId}")
    List<Judgement> getJudgementsBySmId(Integer smId);

    @Select("select sp_id,status from sports where sp_id=#{spId}")
    Judgement getSportBySpId(Integer spId);

    /*@Select("select gid, name, sp_id from sp_groups where sp_id=#{spId}")
    public List<Group> getGroupsBySpId(Integer spId);*/

    @Select("select rid, name, units,is_rank_mode, mappings from judge_rules where sm_id=#{smId}")
    List<JudgeRuleB> getJudgeRulesBySmId(Integer smId);

    int addJudgeRule(JudgeRuleB judgeRule, Integer smId);

    int modifyJudgeRule(JudgeRuleB judgeRule);

    @Delete("delete from judge_rules where rid=#{rid}")
    void deleteJudgeRule(Integer rid);

    @Update("update sports set rid=#{rid} where sp_id=#{spId}")
    void updateCompJudgeRuleByRid(Integer spId,Integer rid);

    @Update("update application_sports set score=#{scoreS} where sp_id=#{spId} and pid=#{pid}")
    void addAppScore(Score score);

    @Select("select aid,score,sp_id from application_sports where sp_id=#{spId}")
    List<ApplicationSport> getAppsBySpId(Integer spId);

    void addRankAndDegree(ApplicationSport app);

    @Select("select units, mappings, is_rank_mode from judge_rules where rid=(select rid from sports where sp_id=#{spId})")
    JudgeRuleB getJudgeRuleBySpId(Integer spId);

}
