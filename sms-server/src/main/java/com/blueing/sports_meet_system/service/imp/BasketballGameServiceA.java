package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.BasketballGame;
import com.blueing.sports_meet_system.service.BasketballGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class BasketballGameServiceA implements BasketballGameService {
    @Autowired
    private BasketballGameMapper basketballGameMapper;

    @Autowired
    private WebSocketServer socketServer;


    @Override
    public void addfraction(Integer teId, Integer fraction) {
        BasketballGame basketballGame = basketballGameMapper.querySpidScores(teId);
        Integer score = basketballGame.getScore();
        score = score + fraction;
        basketballGame.setScore(score);
        basketballGameMapper.modifyTeamScores(teId, score);
        ZonedDateTime zonedNow = ZonedDateTime.now();
        basketballGameMapper.addScoringSituation(teId, zonedNow, fraction);
        socketServer.sendToAllClient(basketballGame.getSpId(),teId);
    }

    @Override
    public void addbasketballGame(String nameA, String rgbA, String nameB, String rgbB,
                                  ZonedDateTime startTime1, ZonedDateTime endTime1,
                                  ZonedDateTime startTime2, ZonedDateTime endTime2,
                                  ZonedDateTime startTime3, ZonedDateTime endTime3,
                                  ZonedDateTime startTime4, ZonedDateTime endTime4) {

        Integer spId = basketballGameMapper.addBasketballs(startTime1, endTime4);
        basketballGameMapper.addBasDuration(spId, startTime1, endTime1, 1);
        basketballGameMapper.addBasDuration(spId, startTime2, endTime2, 2);
        basketballGameMapper.addBasDuration(spId, startTime3, endTime3, 3);
        basketballGameMapper.addBasDuration(spId, startTime4, endTime4, 4);
        basketballGameMapper.addContingent(spId, nameA, rgbA);
        basketballGameMapper.addContingent(spId, nameB, rgbB);
    }

    @Override
    public List<BasketballGame> queryTeamScoringDetailsRecord(Integer spId) {
        List<BasketballGame> contingents = basketballGameMapper.queryContingent(spId);
        for (BasketballGame contingent : contingents) {
            Integer teId = contingent.getTeId();
            contingent.setBasketballGames(basketballGameMapper.queryScoreRecords(teId));
        }
        return contingents;
    }

    @Override
    public List<BasketballGame> queryTeamScores(Integer spId) {
        return basketballGameMapper.queryTeamScores(spId);
    }

    @Override
    public List<BasketballGame> queryBasketballEvent() {
        return basketballGameMapper.queryBasketballEvent();
    }

    @Override
    public List<BasketballGame> queryAiContingent(Integer spId) {
        return basketballGameMapper.queryAiContingent(spId);
    }

    @Override
    public BasketballGame querySchedule(Integer spId) {
        List<BasketballGame> schedules = basketballGameMapper.querySchedule(spId);
        BasketballGame basketballGame = new BasketballGame();
        basketballGame.setSpId(spId);
        basketballGame.setBasketballGames(schedules);
        return basketballGame;
    }
}
