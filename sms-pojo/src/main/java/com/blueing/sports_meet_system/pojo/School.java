package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class School {
    private Integer scId;
    private Integer smId;
    private String name;
    private String teamNumber;
    private String slogan;
    private String img;
    private Integer playerCount;
    private Integer appCount;
}
