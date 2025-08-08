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

@Slf4j
@Service
public class CheckRecordRecordServiceA implements CheckRecordService {
    @Autowired
    private CheckRecordMapper checkRecordMapper;

    @Override
    public CheckRecord queryCheckRecord(Integer smId, Integer spId, Integer eventType) {
        CheckRecord checkRecord = new CheckRecord();
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
            Group group = new Group(0,"田赛",null,null,fieldPlayersList);
            List<Group> groupList = new ArrayList<>();
            groupList.add(group);
            checkRecord.setGroupsList(groupList);
        }
        return checkRecord;
    }
}
