package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.exception.businessEception.LowPower;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.StaffPowerMapper;
import com.blueing.sports_meet_system.pojo.InviteCode;
import com.blueing.sports_meet_system.pojo.Player;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.service.StaffPowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class StaffPowerServiceA implements StaffPowerService {

    @Autowired
    private StaffPowerMapper staffPowerMapper;

    @Override
    public List<User> getStaff() {
        User currentUser= Interceptor.getCurrentUser();
        return staffPowerMapper.getUserByPower(currentUser.getSmId());
    }

    @Override
    public void modifyUserSmPowerDegree(User user){
        user.setSmId(Interceptor.getCurrentUser().getSmId());
        staffPowerMapper.modifyUserSm(user);
    }

    @Override
    public List<InviteCode> getInviteCodes() {
        User currentUser= Interceptor.getCurrentUser();
        List<InviteCode> inviteCodes = staffPowerMapper.getInviteCodes(currentUser.getSmId());
        inviteCodes.forEach(StaffPowerServiceA::toArray);
        return inviteCodes;
    }

    public static void toArray(InviteCode inviteCode) {
        String powerDegree = inviteCode.getPowerDegree();
        String[] split = powerDegree.substring(1, powerDegree.length()-1).split(", ");
        List<Integer> userType = Arrays.stream(split).map(Integer::parseInt).toList();
        inviteCode.setUserType(userType);
    }


    @Override
    public void addInviteCode(InviteCode inviteCode) {
        User currentUser= Interceptor.getCurrentUser();
        if((inviteCode.getUserType().contains(4))||currentUser.getUserType().contains(5)){
//            log.info("测试机哦");
            //仅当包含邀请码权限为4时候会对项目负责人网开一面（有一点不安全，就是只要我是团体负责人，我可以邀请任何团体的人）
            inviteCode.setPowerDegree(inviteCode.getUserType().toString());
            staffPowerMapper.addInviteCode(inviteCode,currentUser.getSmId());
        }else{
            throw new LowPower();
        }
    }

    @Override
    public void modifyInviteCode(InviteCode inviteCode) {
        inviteCode.setPowerDegree(inviteCode.getUserType().toString());
        staffPowerMapper.modifyInviteCode(inviteCode);
    }

}
