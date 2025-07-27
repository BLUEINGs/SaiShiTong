package com.blueing.sports_meet_system.service.imp;


import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.DepartmentMapper;
import com.blueing.sports_meet_system.mapper.JudgeMapper;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.mapper.SportMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.SportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SportServiceA implements SportService {


    @Autowired
    private SportMapper sportGroupMapper;

    @Autowired
    private MyInfoMapper myInfoMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private JudgeMapper judgeMapper;

    @Override
    public List<SportGroup> getAllSport() {
        Integer smId = Interceptor.getCurrentUser().getSmId();
        List<Sport> sports = sportGroupMapper.getSports(smId);
        List<SportGroup> sportGroups=new ArrayList<>();
        Map<Integer,List<Sport>> sportGroupMap=new HashMap<>();
        for (Sport sport : sports) {
            Integer mainSpId = sport.getMainSpId();
            if(sportGroupMap.containsKey(mainSpId)){
                sportGroupMap.get(mainSpId).add(sport);
            }else{
                ArrayList<Sport> sports0 = new ArrayList<>();
                sports0.add(sport);
                sportGroupMap.put(mainSpId,sports0);
            }
        }

        for (List<Sport> sportList : sportGroupMap.values()) {
            SportGroup sportGroup = new SportGroup(sportList.getFirst());
            sportGroup.setSports(sportList);
            sportGroups.add(sportGroup);
        }
        return sportGroups;
    }


    @Override
    public List<Group> getGroups(Integer spId) {
        User currentUser = Interceptor.getCurrentUser();
        List<Group> groups = sportGroupMapper.getGroups(spId);
        if (groups.isEmpty()) {
            groups.add(new Group(-1, "当前比赛未分组", spId, currentUser.getSmId(), sportGroupMapper.getPlayersBySpId(spId)));
        } else {
            for (Group group : groups) {
                group.setPlayers(sportGroupMapper.getPlayersByGid(group.getGid()));
            }
            List<Player> noArrangePlayers = sportGroupMapper.getPlayersBySpIdAndNull(spId);
            if(!noArrangePlayers.isEmpty()){
                Group group = new Group(null,"待分组选手",null,null,noArrangePlayers);
                groups.add(group);
            }
        }
        List<Player> judges = judgeMapper.getJudgesBySpId(spId);
        groups.addFirst(new Group(0,"裁判",spId,null,judges));
//        groups.add()
        return groups;
    }

    /*@Override
    public Integer addSportGroup(){
        return sportGroupMapper.addSportGroup(addSportGroup(sportGroup));
    }*/

    @Override
    public Integer deleteSportGroup(Integer mainSpId) {
        return sportGroupMapper.deleteSportGroup(mainSpId);
    }

    @Override
    @Transactional
    public void addSportGroup(SportGroup sportGroup) {
        User currentUser = Interceptor.getCurrentUser();
        Integer spId = null;
        String name = sportGroup.getName();
        Integer compSystem = sportGroup.getCompSystem();
        ZonedDateTime appStartTime = sportGroup.getAppStartTime();
        ZonedDateTime appEndTime = sportGroup.getAppEndTime();
        Integer eventType = sportGroup.getEventType();
        Integer subEventType = sportGroup.getSubEventType();
        for (int i = 0; i < sportGroup.getSports().size(); i++) {
            Sport sport = sportGroup.getSports().get(i);
            sport.setName(name);
            sport.setCompSystem(compSystem);
//            sport.setCompType(i + 1);
            sport.setEventType(eventType);
            sport.setSubEventType(subEventType);
            sport.setSize(sportGroup.getSize());
            sport.setGender(sportGroup.getGender());
            if (i == 0) {
                sport.setAppStartTime(appStartTime);
                sport.setAppEndTime(appEndTime);
                sportGroupMapper.addSport(sport, currentUser.getSmId());
                spId = sport.getSpId();
                sport.setMainSpId(spId);
                sportGroupMapper.setSportMainSpId(sport);
                sport.setRid(sportGroup.getRid());
            } else {
                sport.setMainSpId(spId);
                sportGroupMapper.addSport(sport, currentUser.getSmId());
            }
        }
    }

    @Override
    @Transactional
    public void modifySportGroup(SportGroup sportGroup) {
        List<Sport> sports = sportGroup.getSports();
        Integer compSystem = sportGroup.getCompSystem();
        String name = sportGroup.getName();
        ZonedDateTime appStartTime = sportGroup.getAppStartTime();
        ZonedDateTime appEndTime = sportGroup.getAppEndTime();
        for (int i = 0; i < sports.size(); i++) {
            Sport sport = sports.get(i);
            sport.setName(name);
            sport.setCompSystem(compSystem);
            sport.setMainSpId(sportGroup.getMainSpId());
            sport.setSize(sportGroup.getSize());
            sport.setGender(sportGroup.getGender());
            sport.setRid(sportGroup.getRid());
            if (i == 0) {
                sport.setAppStartTime(appStartTime);
                sport.setAppEndTime(appEndTime);
            }
            log.info(sport.toString());
            sportGroupMapper.modifySport(sport);
        }
    }

    @Override
    public void signUpSport(Integer scId, Integer spId){
        User currentUser = Interceptor.getCurrentUser();
        UserSm userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), currentUser.getSmId());
        Player player = myInfoMapper.getOnlyPlayer(currentUser.getUid(), currentUser.getSmId(),scId);
        currentUser.setPowerDegree(userSm.getPowerDegree());
        if(!currentUser.getUserType().contains(4)){
            throw new RuntimeException("请先以\"运动员\"身份加入团体后再报名");
        }
        ArrayList<ApplicationSport> applicationSports = new ArrayList<>();
        ApplicationSport app = new ApplicationSport(null, spId, null, ZonedDateTime.now(), null);
        app.setPid(player.getPid());
        applicationSports.add(app);
        departmentMapper.addApplications(applicationSports,currentUser.getSmId());
    }

    @Override
    public void cancelSport(Integer scId, Integer spId){
        User currentUser = Interceptor.getCurrentUser();
        Player player = myInfoMapper.getOnlyPlayer(currentUser.getUid(), currentUser.getSmId(),scId);
        List<PidAndSpId> pidAndSpIds=new ArrayList<>();
        pidAndSpIds.add(new PidAndSpId(player.getPid(),spId));
        departmentMapper.deleteApplications(pidAndSpIds);
    }

}
