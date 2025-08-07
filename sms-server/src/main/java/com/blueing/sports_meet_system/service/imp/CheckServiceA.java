package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.CheckMapper;
import com.blueing.sports_meet_system.pojo.Check;
import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Player;
import com.blueing.sports_meet_system.service.CheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CheckServiceA implements CheckService {
    @Autowired
    private CheckMapper checkMapper;

    @Override
    public Check listCheck(Integer smId, Integer spId) {
        Check check = checkMapper.listCheck(smId, spId);
        List<Group> groups = checkMapper.listGroup(smId, spId);
        for (Group group : groups) {
            Integer gid = group.getGid();
            List<Player> players = checkMapper.listPlayer(smId, spId, gid);
            for (Player player : players) {
                Integer pid = player.getPid();
                Player player1 = checkMapper.addPlayers(pid);
                player.setName(player1.getName());
                player.setPClass(player1.getPClass());
            }
            group.setPlayers(players);
        }
        check.setGroups(groups);
        return check;
    }
}
