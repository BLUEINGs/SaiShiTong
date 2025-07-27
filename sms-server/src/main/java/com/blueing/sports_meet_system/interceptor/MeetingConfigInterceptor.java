package com.blueing.sports_meet_system.interceptor;

import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.pojo.UserSm;
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
public class MeetingConfigInterceptor implements HandlerInterceptor {

    private final MyInfoMapper myInfoMapper;

    public MeetingConfigInterceptor(MyInfoMapper myInfoMapper) {
        this.myInfoMapper = myInfoMapper;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User currentUser = Interceptor.getCurrentUser();
        UserSm userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), currentUser.getSmId());
        if(userSm==null){
            returnLowPower(response, currentUser, log, objectMapper);
            return false;
        }
        currentUser.setPowerDegree(userSm.getPowerDegree());
        currentUser.setJoinedSchools(userSm.getJoinedSchools());
        List<Integer> powerDegrees =currentUser.getUserType();

        if(powerDegrees==null|| !(powerDegrees.contains(5)||powerDegrees.contains(2))){
            returnLowPower(response, currentUser, log, objectMapper);
            return false;
        }else{
            return true;
        }
    }

}
