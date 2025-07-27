package com.blueing.sports_meet_system.pojo;

import com.blueing.sports_meet_system.utils.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSm {

    private Integer uid;
    private Integer smId;
    private String name;
    private String powerDegree;
    private List<Integer> joinedSchools;
    private List<Integer> userType;
    private String scIds;

    public void setJoinedSchools(String joinedSchools){
        this.joinedSchools= MapUtil.toIntList(joinedSchools);
        this.scIds=joinedSchools;
    }

    public void setScIds(String  scIds){
        this.scIds=scIds;
        this.joinedSchools= MapUtil.toIntList(scIds);
    }

    public void setPowerDegree(String powerDegree){
        this.powerDegree=powerDegree;
        this.userType=MapUtil.toIntList(powerDegree);
    }

}
