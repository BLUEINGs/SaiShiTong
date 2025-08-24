package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.CheckRecord;
import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Player;

import java.util.List;

public interface CheckService {
    CheckRecord listCheck(Integer smId, Integer spId);
}
