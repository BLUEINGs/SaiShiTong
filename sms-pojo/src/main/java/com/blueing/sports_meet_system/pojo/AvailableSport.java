package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSport {

    private Integer spId;
    private String name;
    private Boolean gender;
    private Integer compSystem;
    private Integer compType;
    private Integer mainSpId;
    private Integer status;

}
