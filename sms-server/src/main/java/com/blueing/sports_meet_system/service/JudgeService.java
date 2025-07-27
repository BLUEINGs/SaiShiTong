package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.Judge;
import com.blueing.sports_meet_system.pojo.Judgement;

import java.util.List;

public interface JudgeService {
    List<Judge> getJudges();

    void assignJudges(Integer spId, List<Integer> judgeUids);

    List<Judgement> getJudgements();
}
