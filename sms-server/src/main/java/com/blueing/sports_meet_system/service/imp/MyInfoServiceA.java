package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.MyInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MyInfoServiceA implements MyInfoService {

    private final MyInfoMapper myInfoMapper;
    private final LoginRegisterServiceA loginRegisterServiceA;

    public MyInfoServiceA(MyInfoMapper myInfoMapper, LoginRegisterServiceA loginRegisterServiceA) {
        this.myInfoMapper = myInfoMapper;
        this.loginRegisterServiceA = loginRegisterServiceA;
    }

    public UserInfo handleUserInfo(){
        User currentUser = Interceptor.getCurrentUser();
        UserInfo userInfo = new UserInfo();
        User user = myInfoMapper.getUserInfoByUid(currentUser.getUid());
        userInfo.setName(user.getName());
        userInfo.setHead(user.getHead());
        userInfo.setSmId(user.getSmId());
        userInfo.setJudgeEvents(user.getJudgeEvents());
        List<Player> players = myInfoMapper.getPlayerByUid(user.getUid(), userInfo.getSmId());
        if(!players.isEmpty()){
            List<AvailableSport> sports = new ArrayList<>();
            List<Integer> pids=new ArrayList<>();
            for (Player player : players) {
//                userInfo.setPids(player.getPid());
                pids.add(player.getPid());
                sports.addAll(myInfoMapper.getSportsByPid(player.getPid()));
                userInfo.setMySports(sports);
            }
            userInfo.setPids(pids);
        }
        List<UserSm> sms = myInfoMapper.getSmsByUid(currentUser.getUid());
        userInfo.setJoinedSportMeetings(sms);
        String powerDegree=null;
        for (UserSm sm : sms) {
            if(sm.getSmId().equals(user.getSmId())){
                powerDegree=sm.getPowerDegree();
                List<Integer> scIds = sm.getJoinedSchools();
                if(scIds!=null&&!scIds.isEmpty()){
                    userInfo.setJoinedSchools(myInfoMapper.getSchoolsByScIds(scIds));
                }
            }
        }
        userInfo.setPowerDegree(powerDegree);
        return userInfo;
    }

    @Override
    public List<Object> modifyUserSmId(Integer smId) throws UserNamePasswordNoExistException {
        User currentUser = Interceptor.getCurrentUser();
        myInfoMapper.modifySmIdByUid(smId,currentUser.getUid());
        return loginRegisterServiceA.login(currentUser.getName(),currentUser.getPassword());
    }

}
