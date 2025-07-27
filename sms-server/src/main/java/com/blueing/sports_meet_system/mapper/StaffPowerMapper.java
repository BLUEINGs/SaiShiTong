package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.InviteCode;
import com.blueing.sports_meet_system.pojo.User;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface StaffPowerMapper {

    @Select("select u.uid, name,us.power_degree from users u left join user_sm us on u.uid=us.uid where us.sm_id=#{smId}")
    List<User> getUserByPower(Integer smId);

    @Select("select code, power_degree, expire_time,user from invite_code where sm_id=#{smId}")
    List<InviteCode> getInviteCodes(Integer smId);

    @Delete("delete from invite_code where code=#{code}")
    void deleteInviteCode(String code);

    @Insert("insert into invite_code(code, power_degree, expire_time, sm_id,user) value(#{code.code},#{code.powerDegree},#{code.expireTime},#{smId},#{code.user})")
    void addInviteCode(InviteCode code,Integer smId);

    @Select("select code, power_degree, expire_time,sm_id,user from invite_code where code=#{code}")
    InviteCode getInviteCodeByCode(String code);

    List<InviteCode> getInviteCodeByUsers(String user);

    @Delete("delete from invite_code where expire_time<#{now}")
    int clearExpiredInviteCode(ZonedDateTime now);

    int modifyInviteCode(InviteCode inviteCode);

    int modifyUserSm(User user);

    @Delete("delete from user_sm where uid=#{uid} and sm_id=#{smId}")
    void deleteUserSm(Integer uid,Integer smId);

}
