package com.blueing.sports_meet_system.pojo;

import com.blueing.sports_meet_system.utils.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private String name;
    private String head;
    private List<Integer> powerDegree;
    private List<Integer> pids;
    private List<UserSm> joinedSportMeetings;
    private List<School> joinedSchools;
    private List<Integer> scIds;
    private Integer smId;
    private List<Integer> judgeEvents;
    private List<AvailableSport> mySports;

    public void setPowerDegree(String powerDegree){
        if(powerDegree==null){
            return;
        }
        this.powerDegree= Arrays.stream(powerDegree.substring(1,powerDegree.length()-1).split(", ")).map(Integer::parseInt).toList();
    }

    public void setScIds(String scIds){
        this.scIds = MapUtil.toIntList(scIds);
    }

}
