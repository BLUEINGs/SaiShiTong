package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.*;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CheckMapper {
    @Select("select pid,gid,track,check_state from application_sports where sp_id = #{spId}")
   List<Check> list(Integer spId);
}
