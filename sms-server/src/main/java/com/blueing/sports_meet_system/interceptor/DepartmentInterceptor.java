package com.blueing.sports_meet_system.interceptor;

import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.pojo.Result;
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

import java.util.ArrayList;
import java.util.List;

import static com.blueing.sports_meet_system.interceptor.Interceptor.returnLowPower;

@Slf4j
@Component
public class DepartmentInterceptor implements HandlerInterceptor {

    private final MyInfoMapper myInfoMapper;

    public DepartmentInterceptor(MyInfoMapper myInfoMapper) {
        this.myInfoMapper = myInfoMapper;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User currentUser = Interceptor.getCurrentUser();
        UserSm userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), currentUser.getSmId());
        currentUser.setPowerDegree(userSm.getPowerDegree());
        if(userSm.getJoinedSchools()!=null){
            currentUser.setJoinedSchools(userSm.getJoinedSchools());
        }else{
            currentUser.setJoinedSchools(new ArrayList<>());
        }
        List<Integer> powerDegrees = MapUtil.toIntList(currentUser.getPowerDegree());
        currentUser.setUserType(powerDegrees);
//        log.info(powerDegrees.toString());

        if(powerDegrees==null||!(powerDegrees.contains(2) || powerDegrees.contains(5))){
            returnLowPower(response, currentUser, log, objectMapper);
            return false;
        }else{
            return true;
        }
    }

}
