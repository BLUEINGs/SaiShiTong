package com.blueing.sports_meet_system.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeRule {

    private Integer rid;
    private String name;
    private Boolean isRankMode;
    private List<String> units;
    private Map<String,Double> mappings;

}
