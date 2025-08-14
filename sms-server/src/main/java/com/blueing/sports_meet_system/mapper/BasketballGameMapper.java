package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface BasketballGameMapper {
    BasketballGame queryTeamScores(Integer cid);

    void modifyTeamScores(Integer cid,Integer score);

    void addScoringSituation (Integer cid,ZonedDateTime scoringTime, Integer one, Integer two, Integer three );
}
