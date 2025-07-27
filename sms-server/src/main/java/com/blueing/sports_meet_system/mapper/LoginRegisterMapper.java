package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.pojo.UserSm;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginRegisterMapper {

    @Select("select uid,name,head,sc_id,sm_id from users where name=#{userName} and password=#{password}")
    User getUserByLogin(String userName,String password);

    void addUserByRegister(User user);

    void addUserSmByRegister(User user);

}
