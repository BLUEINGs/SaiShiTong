package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailScore {

    private Integer pid;
    private Integer spId;
    private String eventName;
    private Integer compType;
    private String athleteName;
    private String teamName;
    private Boolean gender;
    private Double degree;
    private String score;
    private Integer rank;

}
