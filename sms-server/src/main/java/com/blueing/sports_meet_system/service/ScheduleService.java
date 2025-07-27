package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.Sport;

import java.util.List;

public interface ScheduleService {

    List<Sport> getScheduleList(Integer smId);

    void modifySchedule(Sport sport);

}
