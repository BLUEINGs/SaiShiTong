package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDetail {

    private List<Player> players;
    private Double totalDegree;
    private Double maleDegree;
    private Double femaleDegree;
    private Double extraDegree;
    private List<DetailScore> maleDegreeDetails;
    private List<DetailScore> femaleDegreeDetails;
    private List<DetailScore> extraDegreeDetails;

}
