package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CheckRecordMapper {

    CheckRecord queryCheckRecord(Integer smId, Integer spId);

    List<Group> groupList(Integer smId,Integer spId);


    List<Player> trackPlayerList(Integer smId,Integer spId ,Integer gid);


    List<Player> fieldPlayerList(Integer smId,Integer spId );
}
