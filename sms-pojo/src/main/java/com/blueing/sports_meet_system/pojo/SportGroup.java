package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportGroup {
    private String name;
    private Integer compSystem;
    private Integer mainSpId;
    private Integer rid;
    private Integer eventType;
    private Integer subEventType;
    private String size;
    private Boolean gender;
    private ZonedDateTime appStartTime;
    private ZonedDateTime appEndTime;
    private List<Sport> sports;

    public SportGroup(Sport sport) {
        this(sport.getName(), sport.getCompSystem(),sport.getMainSpId(),sport.getRid(),sport.getEventType(),sport.getSubEventType(), sport.getSize(), sport.getGender(),sport.getAppStartTime(),sport.getAppEndTime(),new ArrayList<>());
    }
}
