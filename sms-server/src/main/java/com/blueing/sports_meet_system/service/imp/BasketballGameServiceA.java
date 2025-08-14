package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.BasketballGame;
import com.blueing.sports_meet_system.service.BasketballGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class BasketballGameServiceA implements BasketballGameService {
    @Autowired
    private BasketballGameMapper basketballGameMapper;


    @Override
    public void addfraction(Integer cid,Integer fraction) {
        BasketballGame basketballGame = basketballGameMapper.queryTeamScores(cid);
        Integer score = basketballGame.getScore();
        score = score + fraction;
        basketballGame.setScore(score);
        basketballGameMapper.modifyTeamScores(cid,score);
        ZonedDateTime zonedNow = ZonedDateTime.now();
        if (fraction == 1) {
            basketballGameMapper.addScoringSituation(cid,zonedNow,1,0,0);
        } else if (fraction == 2) {
            basketballGameMapper.addScoringSituation(cid,zonedNow,0,1,0);
        }else if (fraction == 3) {
            basketballGameMapper.addScoringSituation(cid,zonedNow,0,0,1);
        }
    }
}
