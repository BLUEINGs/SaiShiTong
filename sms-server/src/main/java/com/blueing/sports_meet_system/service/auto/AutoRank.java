package com.blueing.sports_meet_system.service.auto;

import com.blueing.sports_meet_system.mapper.DepartmentMapper;
import com.blueing.sports_meet_system.mapper.IndexMapper;
import com.blueing.sports_meet_system.mapper.ScheduleMapper;
import com.blueing.sports_meet_system.mapper.SportMeetingsMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.utils.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class AutoRank {

    @Autowired
    private IndexMapper indexMapper;

    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private SportMeetingsMapper sportMeetingsMapper;

    @Scheduled(fixedDelay = 60 * 1000 * 5)
    public synchronized void timer() {

        log.info("更新运动会计数数据");
        sportMeetingsMapper.updateCount();

        log.info("自动计算排名开始");
        for (Integer smId : indexMapper.getSportOnGoingMeetingSmIds()) {
            List<School> schoolList = departmentMapper.getSchoolList(smId);
            rankStep1(schoolList,smId);
        }
        log.info("自动计算排名结束");
    }


    private void rankStep1(List<School> schoolList,Integer smId) {
        int meetingRank=1;
        Map<Integer,Double> schoolsTotalDegree0=new HashMap<>();
//        Map<Integer,Double> allPlayersTotalDegree0=new HashMap<>();
        List<List<Map.Entry<Integer, Double>>>  schoolsAndPlayersTotalDegree=new ArrayList<>();//这个集合是把各个班级的学生收集起来
        for (School school : schoolList) {
            //给团体中的每个人赋团体排名/总分
//            schoolDegree.add(school);
            List<DetailScore> scores = departmentMapper.getDetailScoresByScId(school.getScId());
            Map<Integer,Double> playersTotalDegree0=new HashMap<>();
            double schoolTotalDegree=0;
            for (DetailScore score : scores) {
                Double playerDegree = playersTotalDegree0.getOrDefault(score.getPid(), 0.0);
                schoolTotalDegree+=score.getDegree();
                playersTotalDegree0.put(score.getPid(), playerDegree+score.getDegree());
            }
            schoolsTotalDegree0.put(school.getScId(),schoolTotalDegree);
            List<Map.Entry<Integer, Double>> playerTotalDegree=new ArrayList<>(playersTotalDegree0.entrySet());
            playerTotalDegree.sort((o1, o2) -> (int) (o2.getValue()-o1.getValue()));
            /*for (int i = 0; i < playerTotalDegree.size(); i++) {
                Map.Entry<Integer,Double> degree=playerTotalDegree.get(i);
                allPlayersTotalDegree0.put(degree.getKey(),degree.getValue());
                meetingRank++;
            }*/
            schoolsAndPlayersTotalDegree.add(playerTotalDegree);
        }
        //然后生成学生在运动会的总排名
        /*List<Map.Entry<Integer, Double>> allPlayersTotalDegree=new ArrayList<>(allPlayersTotalDegree0.entrySet());
        allPlayersTotalDegree.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
        for (int i = 0; i < allPlayersTotalDegree.size(); i++) {
            Map.Entry<Integer,Double> degree=allPlayersTotalDegree.get(i);
            indexMapper.rankPlayers(degree.getKey(),null,i+1,null,smId);
        }*/
        for (List<Map.Entry<Integer, Double>> players : schoolsAndPlayersTotalDegree) {
            for (int i = 0; i < players.size(); i++) {
                Map.Entry<Integer, Double> degree = players.get(i);
                indexMapper.rankPlayers(degree.getKey(),i+1,meetingRank++,degree.getValue(),smId);
            }
        }

        //然后生成学校分数集合
        List<Map.Entry<Integer, Double>> schoolsTotalDegree=new ArrayList<>(schoolsTotalDegree0.entrySet());
        schoolsTotalDegree.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
        for (int i = 0; i < schoolsTotalDegree.size(); i++) {
            Map.Entry<Integer,Double> degree=schoolsTotalDegree.get(i);
            indexMapper.rankSchools(degree.getKey(),i+1,degree.getValue());
        }
    }

}
