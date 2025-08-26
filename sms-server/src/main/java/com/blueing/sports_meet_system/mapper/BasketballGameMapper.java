package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface BasketballGameMapper {
    BasketballRecords querySpidScores(Integer teId);

    List<BasketballRecords> queryTeamScores(Integer spId);

    void modifyTeamScores(Integer teId,Integer score);

    void addScoringSituation (Integer teId,ZonedDateTime scoringTime, Integer score );

    Integer addBasketballs(ZonedDateTime startTime, ZonedDateTime endTime);

    void addBasDuration(Integer spId,ZonedDateTime startTime, ZonedDateTime endTime,Integer type);

    void addContingent(Integer spId,String name,String rgb);

    List<BasketballRecords> queryContingent(Integer spId);

    List<BasketballRecords> queryScoreRecords(Integer teId);

    List<BasketballRecords> queryScoreRecordsBySpId(Integer spId);

    List<BasketballRecords> queryBasketballEvent();

    List<BasketballRecords> queryAiContingent(Integer spId);

    List<BasketballRecords> querySchedule(Integer spId);

}
