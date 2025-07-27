package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyScore {

    private Integer totalScore;
    private Integer meetingRank;
    private Integer teamRank;
    private String  teamName;
    private List<DetailScore> recentScores;

}
