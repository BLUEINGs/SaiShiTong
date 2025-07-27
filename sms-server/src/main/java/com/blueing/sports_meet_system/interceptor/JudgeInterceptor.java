package com.blueing.sports_meet_system.interceptor;

import com.blueing.sports_meet_system.mapper.JudgeMapper;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.utils.MapUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

import static com.blueing.sports_meet_system.interceptor.Interceptor.returnLowPower;

@Slf4j
@Component
public class JudgeInterceptor implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    private final MyInfoMapper myInfoMapper;
    @Autowired
    private JudgeMapper judgeMapper;

    public JudgeInterceptor(MyInfoMapper myInfoMapper) {
        this.myInfoMapper = myInfoMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User currentUser = Interceptor.getCurrentUser();
//        List<Integer> judgeEvents = myInfoMapper.getUserInfoByUid(currentUser.getUid()).getJudgeEvents();
//        currentUser.setJudgeEvents(judgeEvents.toString());
        currentUser.setJudgeEventsList(judgeMapper.getJudgesByUid(currentUser.getUid()));
        currentUser.setPowerDegree(myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(),currentUser.getSmId()).getPowerDegree());
        List<Integer> powerDegrees = MapUtil.toIntList(currentUser.getPowerDegree());
        currentUser.setUserType(powerDegrees);

        if(powerDegrees==null||!(powerDegrees.contains(3) || powerDegrees.contains(5))){
            returnLowPower(response, currentUser, log, objectMapper);
            return false;
        }else{
            return true;
        }
    }

}
