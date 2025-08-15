package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketballGame {
    private Integer spId;
    private Integer teId;
    private String name;
    private ZonedDateTime scoringTime;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Integer Score;
    private Integer type;
    List<BasketballGame>  basketballGames;
}
