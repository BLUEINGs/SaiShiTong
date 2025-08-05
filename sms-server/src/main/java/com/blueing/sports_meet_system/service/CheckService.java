package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.Check;

import java.util.List;

public interface CheckService {
    List<Check> listCheck(Integer spId);
}
