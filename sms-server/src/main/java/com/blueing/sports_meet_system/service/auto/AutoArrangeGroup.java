package com.blueing.sports_meet_system.service.auto;

import com.blueing.sports_meet_system.mapper.SportMapper;
import com.blueing.sports_meet_system.pojo.ApplicationSport;
import com.blueing.sports_meet_system.pojo.Group;
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
@Transactional
public class AutoArrangeGroup {

    @Autowired
    private SportMapper sportMapper;

    // @Scheduled(fixedDelay = 60 * 1000 * 5)
    public synchronized void timer() {
        log.info("即将开始进行自动分组");
        List<Sport> noArrangeSports = sportMapper.getNoArrangeSports(ZonedDateTime.now());
        //这里直接查询不到不需要分组的别赛
        for (Sport sport : noArrangeSports) {
            arrangeGroup(sport);
        }
        //必须更新不用分组的比赛的状态，不然会导致评分状态异常
        sportMapper.updateFieldSportStatus(ZonedDateTime.now());
        log.info("自动分组完成");
    }

    public void arrangeGroup(Sport sport) {
//        构建组别列表
        log.info("开始构建组别列表");
        Map<Group, List<ApplicationSport>> groups = new HashMap<>();//键为group，值为该group里面的运动员的pid
        Integer playerCount = sport.getPlayerCount();
        if(playerCount ==0){
            log.info("当前没人报名，没法分组");
            return;
        }
        Integer countPgp = sport.getCountPgp();
        int groupCount=0;
        if(countPgp!=null){
             groupCount = (int) Math.round((playerCount * 1.0) / countPgp);
        }
        if(groupCount==0){
            groupCount=1;
        }
        log.info("一共会分{}组",groupCount);
        for (int i = 0; i < groupCount; i++) {
            Group group = new Group(null, "第" + (i + 1) + "组", sport.getSpId(), sport.getSmId(),null);
            sportMapper.addGroup(group);
//            log.info("当前gid值为{}", group.getGid());
            groups.put(group, new ArrayList<>());
//            group.setGid(gid);
        }
        //一人一个分
        log.info("开始枚举每个报名记录并分配");
        List<ApplicationSport> apps = sportMapper.getAppsBySpId(sport.getSpId());
        Collections.shuffle(apps);
        for (ApplicationSport app : apps) {
            Map.Entry<Group, List<ApplicationSport>> group = minGroup(groups);
            tryArrange(0, groups, group, app);
            sportMapper.fullAppGid(app);
        }
        sport.setStatus(3);
        sportMapper.modifySport(sport);

    }

    //给顶一个Map.Entry，将会尝试看该Group是否有同班/同学校，如果有则随机摇一个Group重新执行该方法
    private void tryArrange(int count, Map<Group, List<ApplicationSport>> groups, Map.Entry<Group, List<ApplicationSport>> group, ApplicationSport app) {
//        log.info("当前app的spId值为{}",app.getSpId());
        for (ApplicationSport originalApp : group.getValue()) {//这是在遍历当前组下的报名
//            log.info("进循环了孩子们");
            boolean sameSc = originalApp.getScId().equals(app.getScId());
            //只要是同一个学校就不能分在一起，本来就不可能同班
//            借助随机性随便分配
            if (count < 24 && sameSc) {
                //如果同班/同校，如上。最多递归24次，24次后如果仍然无果，视为无法做到彻底隔离开每班/每学校
                log.info("出现了同班/同校情况");
                List<Map.Entry<Group, List<ApplicationSport>>> groupList = new ArrayList<>(groups.entrySet());
                Collections.shuffle(groupList);
                tryArrange(++count, groups, groupList.get(0), app);
                return;
            }
        }
        app.setGid(group.getKey().getGid());
        group.getValue().add(app);
    }

    private Map.Entry<Group, List<ApplicationSport>> minGroup(Map<Group, List<ApplicationSport>> groups) {
        Map.Entry<Group, List<ApplicationSport>> minGroup = null;
        int minCount = Integer.MAX_VALUE;//不考虑极特殊情况能干超int就离谱
        for (Map.Entry<Group, List<ApplicationSport>> group : groups.entrySet()) {
            int count = group.getValue().size();
            if (count < minCount) {
                minCount = count;
                minGroup = group;
            }
        }
        return minGroup;
    }

}
