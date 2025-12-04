package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface BasketballGameMapper {

    BasketballEvent querySpIdScores(Integer teId);

    List<BasketballEvent> queryTeamScores(Integer spId);

    void modifyTeamScores(Integer teId,Integer score);

    @Update("update basketballs set state=#{status} where sp_id=#{spId}")
    void modifyBasketballStatus(Integer spId,Integer status);

    void addScoringSituation (Integer teId,ZonedDateTime scoringTime, Integer score );

    Integer addBasketballs(BasketballEvent basketballEvent);

    void addBasDuration(Integer spId,ZonedDateTime startTime, ZonedDateTime endTime,Integer type);

    void addContingent(Integer spId,String name,String rgb,Integer score);

    List<BasketballEvent> queryScoreRecords(Integer spId);

    List<BasketballEvent> queryScoreRecordsBySpId(Integer spId);

    List<BasketballEvent> queryBasketballEvent();

    List<TeamColor> queryAiContingent(Integer spId);

    List<BasketballEvent> querySchedule(Integer spId);

    BasketballEvent queryBasketball(Integer spId);

    List<BasketballEvent> queryWillStartBasketball(ZonedDateTime dateTime);

    @Update("update basketballs set results=#{url} where sp_id=#{spId}")
    void setResultLink(String url,Integer spId);

    @Select("select sp_id, name, start_time, end_time, state, rtmp from basketballs where state=3")
    List<BasketballEvent> queryDoneStartBasketball(ZonedDateTime now);

    @Select("select sp_id, name, start_time, end_time, state, rtmp from basketballs where state in (0,3)")
    List<BasketballEvent> queryNeedStartBasketball(ZonedDateTime now);
}
