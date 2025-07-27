package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.DepartmentMapper;
import com.blueing.sports_meet_system.mapper.IndexMapper;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class IndexServiceA implements IndexService {

    @Autowired
    private IndexMapper indexMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private MyInfoMapper myInfoMapper;

    @Override
    public List<SportMeeting> getSportMeetings() {
        List<SportMeeting> sportMeetings = indexMapper.getSportMeetings();
        return sportMeetings;
    }

    @Override
    public Map<String, List<Sport>> getTodaySchedule() {
        User currentUser = Interceptor.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate nextDay = today.plusDays(1);
        List<Sport> recentSchedule = indexMapper.getTodaySchedule(currentUser.getSmId(), today.atTime(0, 0).atZone(ZoneId.of("UTC+08:00")), nextDay.atTime(0, 0).atZone(ZoneId.of("UTC+08:00")));
        Map<String, List<Sport>> todaySchedule = new HashMap<>();
        if (recentSchedule != null) {
            for (Sport sport : recentSchedule) {
                if (ZonedDateTime.now().compareTo(sport.getGameStartTime()) > 0 && ZonedDateTime.now().compareTo(sport.getGameEndTime()) < 0) {
                    List<Sport> ongoing = todaySchedule.getOrDefault("ongoing", new ArrayList<>());
                    ongoing.add(sport);
                    todaySchedule.put("ongoing", ongoing);
                } else if (ZonedDateTime.now().compareTo(sport.getGameStartTime()) < 0) {
                    List<Sport> upcoming = todaySchedule.getOrDefault("upcoming", new ArrayList<>());
                    upcoming.add(sport);
                    todaySchedule.put("upcoming", upcoming);
                } else {
                    List<Sport> passed = todaySchedule.getOrDefault("passed", new ArrayList<>());
                    passed.add(sport);
                    todaySchedule.put("passed", passed);
                }
            }
        }
        return todaySchedule;
    }

    @Override
    public List<MyScore> getMyScores() {
        User currentUser = Interceptor.getCurrentUser();
        List<Player> players = myInfoMapper.getPlayerByUidAndSmId(currentUser.getUid(), currentUser.getSmId());
        if (players.isEmpty()) {
            return null;
        } else {
            List<MyScore> myScores=new ArrayList<>();
            for (Player player : players) {
                MyScore myScore= indexMapper.getTotalScoreByPid(player.getPid());
                if(myScore==null){
                    continue;
                }
                myScore.setRecentScores(indexMapper.getScoresByPid(player.getPid(), currentUser.getSmId()));
                myScores.add(myScore);
            }
            return myScores;
        }
    }

    @Override
    public List<DetailScore> getSchoolScores() {
        User currentUser = Interceptor.getCurrentUser();
        UserSm userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), currentUser.getSmId());
        if (userSm == null||userSm.getJoinedSchools()==null||userSm.getJoinedSchools().isEmpty()) {
            return null;
        }
        return indexMapper.getScoresByScIds(userSm.getJoinedSchools());
    }

}
