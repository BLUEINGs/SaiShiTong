package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.CheckMapper;
import com.blueing.sports_meet_system.pojo.Check;
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
    public List<Check> listCheck(Integer spId) {
        return checkMapper.list(spId);
    }
}
