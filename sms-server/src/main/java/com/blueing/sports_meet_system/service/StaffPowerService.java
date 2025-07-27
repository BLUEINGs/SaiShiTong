package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.exception.businessEception.InviteCodeDuplicateException;
import com.blueing.sports_meet_system.pojo.InviteCode;
import com.blueing.sports_meet_system.pojo.Player;
import com.blueing.sports_meet_system.pojo.User;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface StaffPowerService {

    List<User> getStaff();

    void modifyUserSmPowerDegree(User user);

    List<InviteCode> getInviteCodes();

    void addInviteCode(InviteCode inviteCode);

    void modifyInviteCode(InviteCode inviteCode);

}
