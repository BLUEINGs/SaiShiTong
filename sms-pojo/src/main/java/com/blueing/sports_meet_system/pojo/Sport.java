package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sport {

    private Integer spId;
    private Integer mainSpId;
    private Integer rid;
    private String name;
    private Integer eventType;
    private Integer subEventType;
    private Boolean gender;
    private String size;
    private Integer compType;
    private Integer compSystem;
    private Integer playerCount;
    private Integer countPgp;
    private Boolean riseType;
    private Integer riseCount;
    private ZonedDateTime appStartTime;
    private ZonedDateTime appEndTime;
    private ZonedDateTime gameStartTime;
    private ZonedDateTime gameEndTime;
    private String venue;
    private Integer status;
    private Integer smId;

    public Sport(Integer compType, Integer countPgp, String venue, Boolean riseType, Integer riseCount) {
        this.compType = compType;
        this.countPgp = countPgp;
        this.venue = venue;
        this.riseType = riseType;
        this.riseCount = riseCount;
    }
}
