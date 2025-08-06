package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.*;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CheckMapper {

    Check listCheck(Integer smId,Integer spId);

    List<Group> listGroup(Integer smId,Integer spId);


    List<Player> listPlayer(Integer smId,Integer spId ,Integer gid);


    Player addPlayers(Integer pid);
}
