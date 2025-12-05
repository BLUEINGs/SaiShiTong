package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.BasketballEvent;
import com.blueing.sports_meet_system.pojo.TeamColor;
import com.blueing.sports_meet_system.service.BasketballGameService;
import com.blueing.sports_meet_system.service.ws.ScoreUpdateServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class BasketballGameServiceA implements BasketballGameService {
    @Autowired
    private BasketballGameMapper basketballGameMapper;

    @Autowired
    private ScoreUpdateServer socketServer;
    @Autowired
    private StreamDetectionService streamDetectionService;


    @Override
    @Transactional
    public void addFraction(Integer teId, Integer fraction) {
        BasketballEvent basketballEvent = basketballGameMapper.querySpIdScores(teId);
        Integer score = basketballEvent.getScore();
        score = score + fraction;
        basketballEvent.setScore(score);
        basketballGameMapper.modifyTeamScores(teId, score);
        ZonedDateTime zonedNow = ZonedDateTime.now();
        basketballGameMapper.addScoringSituation(teId, zonedNow, fraction);
        socketServer.sendToAllClient(basketballEvent.getSpId());
    }

    @Transactional
    @Override
    public void addBasketballGame(String title, String nameA, String rgbA, String nameB, String rgbB, String rtmp,
                                  ZonedDateTime startTime1, ZonedDateTime endTime1,
                                  ZonedDateTime startTime2, ZonedDateTime endTime2,
                                  ZonedDateTime startTime3, ZonedDateTime endTime3,
                                  ZonedDateTime startTime4, ZonedDateTime endTime4) {

        BasketballEvent basketballEvent = BasketballEvent.builder().name(title).startTime(startTime1).endTime(endTime4).rtmp(rtmp).build();
        basketballGameMapper.addBasketballs(basketballEvent);
        Integer spId=basketballEvent.getSpId();
        basketballGameMapper.addBasDuration(spId, startTime1, endTime1, 1);
        basketballGameMapper.addBasDuration(spId, startTime2, endTime2, 2);
        basketballGameMapper.addBasDuration(spId, startTime3, endTime3, 3);
        basketballGameMapper.addBasDuration(spId, startTime4, endTime4, 4);
        basketballGameMapper.addContingent(spId, nameA, rgbA,0);
        basketballGameMapper.addContingent(spId, nameB, rgbB,0);
        scheduleLaunch();
    }

    @Override
    public List<BasketballEvent> queryTeamScoringDetailsRecord(Integer spId) {
        return basketballGameMapper.queryScoreRecords(spId);
    }

    @Override
    public List<BasketballEvent> queryTeamScores(Integer spId) {
        return basketballGameMapper.queryTeamScores(spId);
    }

    @Override
    public List<BasketballEvent> queryBasketballEvent() {
        return basketballGameMapper.queryBasketballEvent();
    }



    @Override
    public List<TeamColor> queryAiContingent(Integer spId) {
        return basketballGameMapper.queryAiContingent(spId);
    }

    @Override
    public BasketballEvent querySchedule(Integer spId) {
        List<BasketballEvent> schedules = basketballGameMapper.querySchedule(spId);
        BasketballEvent basketballEvent = new BasketballEvent();
        basketballEvent.setSpId(spId);
        basketballEvent.setList(schedules);
        return basketballEvent;
    }

    @Transactional
    @Scheduled(fixedDelay = 60 * 1000)
    public void scheduleLaunch(){
        List<BasketballEvent> basketballEvents = basketballGameMapper.queryNeedStartBasketball(ZonedDateTime.now());
        for (BasketballEvent basketballEvent : basketballEvents) {
            //为每一场比赛开启检测任务
            log.info("spId:{}开始检测任务", basketballEvent.getSpId());
            streamDetectionService.pullAndPush(basketballEvent.getState(), basketballEvent.getRtmp(), basketballEvent.getSpId());
            switch (basketballEvent.getState()){
                case 0 -> basketballGameMapper.modifyBasketballStatus(basketballEvent.getSpId(), 1);
                case 3 -> basketballGameMapper.modifyBasketballStatus(basketballEvent.getSpId(),4);
            }
        }
    }

    public BasketballEvent queryEventInfo(Integer spId) {
        return basketballGameMapper.queryBasketball(spId);
    }

}
