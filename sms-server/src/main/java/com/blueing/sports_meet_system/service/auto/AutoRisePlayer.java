package com.blueing.sports_meet_system.service.auto;

import com.blueing.sports_meet_system.mapper.DepartmentMapper;
import com.blueing.sports_meet_system.mapper.JudgeMapper;
import com.blueing.sports_meet_system.mapper.ScheduleMapper;
import com.blueing.sports_meet_system.mapper.SportMapper;
import com.blueing.sports_meet_system.pojo.ApplicationSport;
import com.blueing.sports_meet_system.pojo.Sport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class AutoRisePlayer {

    @Autowired
    private SportMapper sportMapper;

    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private AutoArrangeGroup autoArrangeGroup;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private JudgeMapper judgeMapper;

    @Transactional
    @Scheduled(fixedDelay = 60 * 1000 * 5)
    public synchronized void timer() {
        log.info("即将获取未晋级比赛");
        List<Sport> sports = sportMapper.getNoRisenSports(ZonedDateTime.now());
        for (Sport sport : sports) {
            log.info("当前比赛的性别:{}",sport.getGender());
            if (sport.getRiseType()) {
                sportRisePer(sport);
            } else {
                sportRiseGro(sport);
            }
        }
    }

    public void sportRisePer(Sport sport) {
        List<ApplicationSport> players = sportMapper.getNoRisenPlayersBySpId(sport.getSpId());
        log.info(players.toString());
        if (players.isEmpty()) {
            return;
        }
        players.sort(Comparator.comparingInt(ApplicationSport::getARank));

        Integer riseCount = sport.getRiseCount();
        List<ApplicationSport> risenPlayers = players;
        if (players.size() > riseCount) {
            risenPlayers = players.subList(0, riseCount);
        }//动态处理！：如果人数还不够晋级人数，就全部拉出去晋级
        riseSport(sport, risenPlayers);
    }

    public void sportRiseGro(Sport sport) {
        List<ApplicationSport> players = sportMapper.getNoRisenPlayersBySpId(sport.getSpId());
        if (players.isEmpty()) {
            return;
        }
        Map<Integer, List<ApplicationSport>> groups = new HashMap<>();
        for (ApplicationSport player : players) {
            Integer gid = player.getGid();
            if (groups.containsKey(gid)) {
                groups.get(gid).add(player);
            } else {
                ArrayList<ApplicationSport> groupPlayers = new ArrayList<>();
                groupPlayers.add(player);
                groups.put(gid, groupPlayers);
            }
        }

        Integer riseCount = sport.getRiseCount();
        List<ApplicationSport> risenPlayers = players;
        for (List<ApplicationSport> group : groups.values()) {
            if (riseCount < group.size()) {
                risenPlayers.addAll(group.subList(0, riseCount));
            } else {
                risenPlayers.addAll(group);
            }
        }
        riseSport(sport, risenPlayers);
    }

    private void riseSport(Sport sport, List<ApplicationSport> risenPlayers) {
        risenPlayers.forEach(applicationSport -> {
            applicationSport.setSpId(sportMapper.getNextSportSpId(sport));
            applicationSport.setAppTime(ZonedDateTime.now());
        });
        departmentMapper.addApplications(risenPlayers, sport.getSmId());
        sport.setStatus(5);
        log.info("即将更新当前晋级后比赛的状态值");
        sportMapper.modifySport(sport);
        /*log.info("即将更新下一场比赛数据");
        sportMapper.updateNextSportStatusBySport(sport);*/
        log.info("直接开始更新分组下一场比赛");
        departmentMapper.updateSportPlayerCountByMainSpId(sport.getSpId());
        autoArrangeGroup.arrangeGroup(sportMapper.getNextSport(sport));
    }

}
