package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface BasketballGameMapper {

    Basketball querySpIdScores(Integer teId);

    List<Basketball> queryTeamScores(Integer spId);

    void modifyTeamScores(Integer teId,Integer score);

    @Update("update basketballs set state=#{status} where sp_id=#{spId}")
    void modifyBasketballStatus(Integer spId,Integer status);

    void addScoringSituation (Integer teId,ZonedDateTime scoringTime, Integer score );

    Integer addBasketballs(ZonedDateTime startTime, ZonedDateTime endTime);

    void addBasDuration(Integer spId,ZonedDateTime startTime, ZonedDateTime endTime,Integer type);

    void addContingent(Integer spId,String name,String rgb);

    List<Basketball> queryScoreRecords(Integer spId);

    List<Basketball> queryScoreRecordsBySpId(Integer spId);

    List<Basketball> queryBasketballEvent();

    List<Basketball> queryAiContingent(Integer spId);

    List<Basketball> querySchedule(Integer spId);

    Basketball queryBasketball(Integer spId);

    List<Basketball> queryWillStartBasketball(ZonedDateTime dateTime);

}
