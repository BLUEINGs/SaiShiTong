package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MyInfoMapper {

    @Select("select uid, name, head, sc_id, sm_id,judge_events from users where uid=#{uid}")
    User getUserInfoByUid(Integer uid);

    @Select("select pid, age, gender, p_class, uid, sc_id, number, sports from players where uid=#{uid} and sm_id=#{smId}")
    List<Player> getPlayerByUid(Integer uid,Integer smId);

    @Select("select pid from players where uid=#{uid} and sm_id=#{smId}")
    List<Player> getPlayerByUidAndSmId(Integer uid,Integer smId);

    @Select("select pid from players where uid=#{uid} and sm_id=#{smId} and sc_id=#{scId}")
    Player getOnlyPlayer(Integer uid,Integer smId,Integer scId);

    List<UserSm> getSmsByUid(Integer uid);

    @Select("select uid, sm_id,joined_schools, power_degree from user_sm where uid=#{uid} and sm_id=#{smId}")
    UserSm getUserSmByUidAndSmId(Integer uid,Integer smId);

    @Select("select a.sp_id,s.name,s.gender,s.comp_type,s.main_sp_id,s.status from application_sports a left join players p on a.pid=p.pid left join sports_meet.sports s on a.sp_id=s.sp_id where a.pid=#{pid}")
    List<AvailableSport> getSportsByPid(Integer pid);

    @Update("update users set sm_id=#{smId} where uid=#{uid}")
    void modifySmIdByUid(Integer smId,Integer uid);

    List<School> getSchoolsByScIds(List<Integer> scIds);

}
