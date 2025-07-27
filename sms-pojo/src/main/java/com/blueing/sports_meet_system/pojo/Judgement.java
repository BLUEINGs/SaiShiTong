package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Judgement {

    private Integer spId;
    private String name;
    private Integer status;
    private Boolean gender;
    private Integer compType;
    private Integer eventType;
    private ZonedDateTime gameStartTime;
    private String venue;
    private Integer rid;


}
