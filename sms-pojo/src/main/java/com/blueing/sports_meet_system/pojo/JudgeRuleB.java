package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeRuleB {

    private Integer rid;
    private String name;
    private Boolean isRankMode;
    private String units;
    private String mappings;

}
