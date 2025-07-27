package com.blueing.sports_meet_system.service.auto;

import com.blueing.sports_meet_system.mapper.StaffPowerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
public class CleanInviteCode {

    @Autowired
    StaffPowerMapper staffPowerMapper;

    @Scheduled(fixedDelay=60*1000*5)
    public void clearInviteCode(){
        log.info("即将清理过期邀请码");
        int count = staffPowerMapper.clearExpiredInviteCode(ZonedDateTime.now());
        log.info("本次共清理{}个过期邀请码",count);
    }

}
