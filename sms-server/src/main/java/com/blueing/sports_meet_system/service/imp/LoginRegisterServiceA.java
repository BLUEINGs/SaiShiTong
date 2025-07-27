package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.exception.businessEception.InviteCodeException;
import com.blueing.sports_meet_system.exception.businessEception.UserNameDuplicateException;
import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.LoginRegisterMapper;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.mapper.StaffPowerMapper;
import com.blueing.sports_meet_system.pojo.InviteCode;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.pojo.UserSm;
import com.blueing.sports_meet_system.service.LoginRegisterService;
import com.blueing.sports_meet_system.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LoginRegisterServiceA implements LoginRegisterService {

    @Autowired
    LoginRegisterMapper loginRegisterMapper;
    @Autowired
    private StaffPowerMapper staffPowerMapper;
    @Autowired
    private MyInfoMapper myInfoMapper;
    @Autowired
    private DepartmentServiceA departmentServiceA;

    @Override
    public List<Object> login(String userName, String password) throws UserNamePasswordNoExistException {
        User user = loginRegisterMapper.getUserByLogin(userName, password);
        if (user == null) {
            throw new UserNamePasswordNoExistException("用户名或密码错误");
        }
        List<UserSm> sms = myInfoMapper.getSmsByUid(user.getUid());
        for (UserSm sm : sms) {
            if (sm.getSmId().equals(user.getSmId())) {
                user.setPowerDegree(sm.getPowerDegree());
            }
        }
        List<Object> data = new ArrayList<>();
        List<String> verify = new ArrayList<>();
        long validTime = System.currentTimeMillis() + 1000 * 72 * 60 * 60;
        verify.add(JwtUtil.generateJwt(userName, password, user.getUid(), user.getSmId(), new Date(validTime)));
        verify.add(Long.toString(validTime));
        data.add(verify);
        data.add(user);
        return data;
    }

    @Transactional
    @Override
    public void register(User user, String inviteCode, String powerDegree) {
        User currentUser = Interceptor.getCurrentUser();
        UserSm userSm;
        if(currentUser!=null){
             userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), currentUser.getSmId());
        }else{
            log.info("当前注册用户没有登录，故构建空User");
            currentUser=new User();
            Interceptor.setCurrentUser(currentUser);
            userSm=null;
        }
        //如果是自主邀请码注册：
        //这里权限不足是不含有5这个权限
        if (userSm == null && inviteCode != null) {
            log.info("加入团体+邀请码注册");
            InviteCode inviteCodeD = staffPowerMapper.getInviteCodeByCode(inviteCode);
            if (inviteCodeD == null) {
                throw new InviteCodeException("邀请码不存在");
            }
            StaffPowerServiceA.toArray(inviteCodeD);
            for (Integer i : user.getUserType()) {
                if (!inviteCodeD.getUserType().contains(i)) {
                    throw new InviteCodeException("邀请码权限不足，请联系相关负责人");
                }
            }
            user.setSmId(inviteCodeD.getSmId());
            user.setPowerDegree(inviteCodeD.getPowerDegree());
        } else if (userSm != null && userSm.getUserType().contains(5)) {
            //如果是运动会操办人注册
            log.info("运动会操办人注册");
            user.setSmId(currentUser.getSmId());
            user.setPowerDegree(powerDegree);//如果是运动会操办人注册这里就直接就是传来权限字符串
            //powerDegree会被接收，不用在意
        }
        try {
            loginRegisterMapper.addUserByRegister(user);
        } catch (Exception e) {
            throw new UserNameDuplicateException();
        }
        log.info("目前是LoginRegister中{}", currentUser);
        log.info("这个用户是{}", user);
        //后续处理：需要基于一些权限
        if(user.getSmId()==null||inviteCode==null){
            log.info("独立注册");
            //如果smId没有值，那就说明上面两种都没走。即为独立注册
            return;
        }
        loginRegisterMapper.addUserSmByRegister(user);
        try {
            currentUser.setUid(user.getUid());//不处理，这里不可能是null
            currentUser.setSmId(user.getSmId());
            departmentServiceA.joinSchool(inviteCode);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("当前邀请码没有指定团体，只有指定运动会。无伤大雅，OK");
        }
    }

}
