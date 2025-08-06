package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.*;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CheckMapper {
    @Select("select check_begin_time,check_end_time,venue from sports where sm_id = #{smId} and sp_id = #{spId}")
    Check listCheck(Integer smId,Integer spId);

    @Select("select gid,name from sp_groups where sm_id = #{smId} and sp_id = #{spId}")
    List<Group> listGroup(Integer smId,Integer spId);

    @Select("select pid,track,check_state from application_sports where sm_id = #{smId} and sp_id = #{spId} and gid= #{gid}")
    List<Player> listPlayer(Integer smId,Integer spId ,Integer gid);

    @Select("select name,p_class from players where pid = #{pid}")
    Player addPlayers(Integer pid);
}
