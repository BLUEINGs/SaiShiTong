package com.blueing.sports_meet_system.service;


import com.blueing.sports_meet_system.pojo.BasketballEvent;
import com.blueing.sports_meet_system.pojo.TeamColor;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

public interface BasketballGameService {
    void addFraction(Integer teId, Integer fraction);

    @Transactional
    void addBasketballGame(String title, String nameA, String rgbA, String nameB, String rgbB, String rtmp,
                           ZonedDateTime startTime1, ZonedDateTime endTime1,
                           ZonedDateTime startTime2, ZonedDateTime endTime2,
                           ZonedDateTime startTime3, ZonedDateTime endTime3,
                           ZonedDateTime startTime4, ZonedDateTime endTime4);

    List<BasketballEvent> queryTeamScoringDetailsRecord(Integer spId);

    List<BasketballEvent> queryTeamScores(Integer spId);

    List<BasketballEvent> queryBasketballEvent();

    List<TeamColor> queryAiContingent(Integer spId);

    BasketballEvent querySchedule(Integer spId);
}
