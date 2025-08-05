package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private Integer pid;
    private Integer smId;
    private String name;
    private String head;
    private String number;
    private Integer uid;
    private Integer userType;
    private Integer scId;
    private Integer age;
    private Boolean gender;
    private String pClass;
    private String score;
    private Integer degree;
    private List<Integer> sports;
    private String sportsString;
    private Integer track;
    private String checkState;

}
