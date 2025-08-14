package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketballGame {
    private Integer spId;
    private Integer cid;
    private String name;
    private ZonedDateTime scoringTime;
    private Integer one;
    private Integer two;
    private Integer three;
    private Integer Score;
}
