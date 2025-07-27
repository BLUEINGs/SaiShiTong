package com.blueing.sports_meet_system.pojo;

import com.blueing.sports_meet_system.utils.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer uid;
    private String name;
    private String password;
    private String head;
    private List<Integer> userType;
    private String powerDegree;
    private List<Integer> joinedSchools;
    private String joinedSchoolsStr;
    private Integer smId;
    private List<Integer> judgeEvents;

    public void setJudgeEvents(String judgeEvents){
        this.judgeEvents = new ArrayList<>(Arrays.stream(judgeEvents.substring(1, judgeEvents.length() - 1).split(", ")).map(Integer::parseInt).toList());
    }

    public void setPowerDegree(String powerDegree){
        this.powerDegree=powerDegree;
        this.userType= MapUtil.toIntList(powerDegree);
//        System.out.println("我被执行了");
    }

    public void setUserType(List<Integer> userType){
        this.userType=userType;
        this.powerDegree=userType.toString();
    }

    /*public void setJudgeEvents(List<Integer> judgeEvents){
        this.judgeEvents=judgeEvents;
    }*/

    public void setUserName(String userName){
        this.name=userName;
    }

    public void setJudgeEventsList(List<Integer> judges) {
        this.judgeEvents=judges;
    }
}
