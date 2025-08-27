package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.Basketball;
import com.blueing.sports_meet_system.service.BasketballGameService;
import com.blueing.sports_meet_system.service.ws.ScoreUpdateServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private RtmpStreamService rtmpStreamService;


    @Override
    public void addfraction(Integer teId, Integer fraction) {
        Basketball basketball = basketballGameMapper.querySpIdScores(teId);
        Integer score = basketball.getScore();
        score = score + fraction;
        basketball.setScore(score);
        basketballGameMapper.modifyTeamScores(teId, score);
        ZonedDateTime zonedNow = ZonedDateTime.now();
        basketballGameMapper.addScoringSituation(teId, zonedNow, fraction);
        socketServer.sendToAllClient(basketball.getSpId());
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
    public List<Basketball> queryTeamScoringDetailsRecord(Integer spId) {
        return basketballGameMapper.queryScoreRecords(spId);
    }

    @Override
    public List<Basketball> queryTeamScores(Integer spId) {
        return basketballGameMapper.queryTeamScores(spId);
    }

    @Override
    public List<Basketball> queryBasketballEvent() {
        return basketballGameMapper.queryBasketballEvent();
    }

    @Override
    public List<Basketball> queryAiContingent(Integer spId) {
        return basketballGameMapper.queryAiContingent(spId);
    }

    @Override
    public Basketball querySchedule(Integer spId) {
        List<Basketball> schedules = basketballGameMapper.querySchedule(spId);
        Basketball basketball = new Basketball();
        basketball.setSpId(spId);
        basketball.setList(schedules);
        return basketball;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void scheduleLaunch(){
        List<Basketball> basketballs = basketballGameMapper.queryWillStartBasketball(ZonedDateTime.now());
        for (Basketball basketball : basketballs) {
            //为每一场比赛开启检测任务
            log.info("spId:{}开始检测任务",basketball.getSpId());
            rtmpStreamService.pullAndPush(basketball.getRtmp(),basketball.getSpId());
            basketballGameMapper.modifyBasketballStatus(basketball.getSpId(), 1);
        }
    }

    // public

}
