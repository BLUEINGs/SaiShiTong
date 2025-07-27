package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteCode {

    private String code;
    private List<Integer> userType;
    private String powerDegree;
    private String user;
    private Integer smId;
    private ZonedDateTime expireTime;

    public void setPowerDegree(String powerDegree){
        if(powerDegree==null){
            return;
        }
        this.powerDegree=powerDegree;
        userType = Arrays.stream(powerDegree.substring(1,powerDegree.length()-1).split(", ")).map(Integer::parseInt).toList();
    }

}
