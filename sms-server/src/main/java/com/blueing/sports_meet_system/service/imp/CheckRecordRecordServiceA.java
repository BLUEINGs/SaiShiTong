package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.CheckRecordMapper;
import com.blueing.sports_meet_system.pojo.CheckRecord;
import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Player;
import com.blueing.sports_meet_system.service.CheckRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class CheckRecordRecordServiceA implements CheckRecordService {
    int[] arr = {1,2,3,4,5,6,7,8};
    int[] brr = new int[arr.length];


    @Autowired
    private CheckRecordMapper checkRecordMapper;

    @Override
    public CheckRecord queryCheckRecord(Integer smId, Integer spId, Integer eventType) {
        CheckRecord checkRecord = null;
        if (eventType == 1) {
            checkRecord = checkRecordMapper.queryCheckRecord(smId, spId);
            List<Group> groupsList = checkRecordMapper.groupList(smId, spId);
            for (Group group : groupsList) {
                Integer gid = group.getGid();
                List<Player> trackplayersList = checkRecordMapper.trackPlayerList(smId, spId, gid);
                group.setPlayers(trackplayersList);
            }
            checkRecord.setGroupsList(groupsList);
        } else if (eventType == 2) {
            checkRecord = checkRecordMapper.queryCheckRecord(smId, spId);
            List<Player> fieldPlayersList = checkRecordMapper.fieldPlayerList(smId, spId);
            Group group = new Group(0, "田赛", null, null, fieldPlayersList);
            List<Group> groupList = new ArrayList<>();
            groupList.add(group);
            checkRecord.setGroupsList(groupList);
        }
        return checkRecord;
    }

    @Override
    public void modifyCheckStatus(Integer smId, Integer spId, Integer pid, Integer checkState) {
        checkRecordMapper.modifyCheckStatus(smId, spId, pid, checkState);
    }

    @Override
    public void addTrack(Integer smId, Integer spId, Integer gid) {
        List<Player> players = checkRecordMapper.trackPlayerList(smId, spId, gid);
        //记录组人数;
        int i = 0;
        for (Player player : players) {
            brr[i] = player.getPid();
            i++;
        }
        Random r = new Random();

        for (int j = 0; j < i+1; j++) {
            int num=r.nextInt(i+1);
            int count=brr[j];
            brr[j] = brr[num];
            brr[num] = count;
        }
        for (int u = i; u >=0; u--) {
            checkRecordMapper.addTrack(smId, spId, brr[u], arr[u]);
        }
    }
}
