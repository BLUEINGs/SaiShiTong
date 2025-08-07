package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.CheckRecordMapper;
import com.blueing.sports_meet_system.pojo.CheckRecord;
import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Player;
import com.blueing.sports_meet_system.service.CheckRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CheckRecordRecordServiceA implements CheckRecordService {
    @Autowired
    private CheckRecordMapper checkRecordMapper;

    @Override
    public CheckRecord queryCheckRecord(Integer smId, Integer spId) {
        CheckRecord checkRecord = checkRecordMapper.queryCheckRecord(smId, spId);
        List<Group> groupsList = checkRecordMapper.groupList(smId, spId);
        for (Group group : groupsList) {
            Integer gid = group.getGid();
            List<Player> playersList = checkRecordMapper.playerList(smId, spId, gid);
            for (Player player : playersList) {
                Integer pid = player.getPid();
                Player player1 = checkRecordMapper.queryPlayers(pid);
                player.setName(player1.getName());
                player.setPClass(player1.getPClass());
            }
            group.setPlayers(playersList);
        }
        checkRecord.setGroups(groupsList);
        return checkRecord;
    }
}
