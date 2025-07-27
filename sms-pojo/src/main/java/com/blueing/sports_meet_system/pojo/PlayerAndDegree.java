package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAndDegree {

    private Integer teamRank;
    private Integer meetingRank;
    private Integer totalScore;
    private List<DetailScore> recentScores;

}
