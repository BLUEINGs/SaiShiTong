package com.blueing.sports_meet_system.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckRecord {
    private ZonedDateTime checkBeginTime;
    private ZonedDateTime checkEndTime;
    private String venue;
    private List<Group> groupsList;
}
