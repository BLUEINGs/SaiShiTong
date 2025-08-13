package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.CheckRecord;

public interface CheckRecordService {
    CheckRecord queryCheckRecord(Integer smId, Integer spId,Integer eventType);

    void modifyCheckStatus(Integer smId,Integer spId,Integer pid,Integer checkState);

    void addTrack(Integer smId,Integer spId,Integer gid);
}
