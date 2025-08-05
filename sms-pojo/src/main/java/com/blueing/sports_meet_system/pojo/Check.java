package com.blueing.sports_meet_system.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Check {
    private Integer gid;
    private Integer pid;
    private String name;
    private Integer track;
    private String checkState;
    private LocalDateTime checkBeginTime;
    private LocalDateTime checkEndTime;
}
