package com.blueing.sports_meet_system.service;


import com.blueing.sports_meet_system.pojo.Basketball;
import java.time.ZonedDateTime;
import java.util.List;

public interface BasketballGameService {
    void addfraction(Integer teId,Integer fraction);
    void addbasketballGame(String nameA,String rgbA, String nameB,String rgbB,
                              ZonedDateTime startTime1,ZonedDateTime endTime1,
                              ZonedDateTime startTime2,ZonedDateTime endTime2,
                              ZonedDateTime startTime3,ZonedDateTime endTime3,
                              ZonedDateTime startTime4,ZonedDateTime endTime4);

    List<Basketball> queryTeamScoringDetailsRecord(Integer spId);

    List<Basketball> queryTeamScores(Integer spId);

    List<Basketball> queryBasketballEvent();

    List<Basketball> queryAiContingent(Integer spId);

    Basketball querySchedule(Integer spId);
}
