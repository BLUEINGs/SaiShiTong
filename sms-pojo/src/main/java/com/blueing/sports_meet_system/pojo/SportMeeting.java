package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportMeeting {

    private Integer smId;
    private String name;
    private String description;
    private Integer sportCount;
    private Integer maxPlayers;
    private Integer maxEvents;
    private String numberConfig;
    private Integer schoolCount;
    private Integer playerCount;
    private Integer status;
    private String img;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private LocalTime amStartTime;
    private LocalTime amEndTime;
    private LocalTime pmStartTime;
    private LocalTime pmEndTime;
    private ZonedDateTime registrationDeadline;

}
