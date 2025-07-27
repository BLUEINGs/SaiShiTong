package com.blueing.sports_meet_system.pojo;

import lombok.Data;

@Data
public class NewMeeting extends SportMeeting {

    private String description;
    private String levelConfig;
    private Integer templateId;

}
