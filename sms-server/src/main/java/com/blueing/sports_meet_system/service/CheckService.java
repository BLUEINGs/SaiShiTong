package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.Check;
import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Player;

import java.util.List;

public interface CheckService {
    Check listCheck(Integer smId, Integer spId);
}
