package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.ScheduleMapper;
import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ScheduleServiceA implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;


    @Override
    public List<Sport> getScheduleList(Integer smId) {
        if(smId==null){
            smId = Interceptor.getCurrentUser().getSmId();
        }
        return scheduleMapper.getArrangedSports(smId);
    }

    @Override
    public void modifySchedule(Sport sport) {
        scheduleMapper.modifySportTime(sport);
    }

}
