package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    private Integer spId;
    private Integer pid;
    private Map<String,Double> score;
    private String scoreS;
    private Double degree;

    public void setScore(Map<String,Double> score){
        this.score=score;
        this.scoreS=score.toString();
    }

}
