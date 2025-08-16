package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface BasketballGameMapper {
    BasketballGame queryScores(Integer teId);

    List<BasketballGame> queryTeamScores(Integer spId);

    void modifyTeamScores(Integer teId,Integer score);

    void addScoringSituation (Integer teId,ZonedDateTime scoringTime, Integer score );

    Integer addBasketballs(ZonedDateTime startTime, ZonedDateTime endTime);

    void addBasDuration(Integer spId,ZonedDateTime startTime, ZonedDateTime endTime,Integer type);

    void addContingent(Integer spId,String name,String rgb);

    List<BasketballGame> queryContingent(Integer spId);

    List<BasketballGame> queryScoreRecords(Integer teId);

    List<BasketballGame> queryBasketballEvent();

    List<BasketballGame> queryAiContingent(Integer spId);

    List<BasketballGame> querySchedule(Integer spId);
}
