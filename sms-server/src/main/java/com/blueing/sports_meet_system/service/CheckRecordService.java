package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.CheckRecord;

public interface CheckRecordService {
    CheckRecord queryCheckRecord(Integer smId, Integer spId);
}
