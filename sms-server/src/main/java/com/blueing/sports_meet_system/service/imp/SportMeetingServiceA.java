package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.*;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.SportMeetingService;
import com.blueing.sports_meet_system.service.auto.AutoArrangeDuration;
import com.blueing.sports_meet_system.service.auto.AutoArrangeGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SportMeetingServiceA implements SportMeetingService {

    private final LoginRegisterServiceA loginRegisterServiceA;
    private final LoginRegisterMapper loginRegisterMapper;
    private final SportMapper sportMapper;

    @Autowired
    private AutoArrangeDuration autoArrangeDuration;

    @Value("${oss.head-portrait.url}")
    private String headPortraitUrl;

    private final StaffPowerMapper staffPowerMapper;
    private final SportMeetingsMapper sportMeetingsMapper;
    private final MyInfoMapper myInfoMapper;
    private final MyInfoServiceA myInfoServiceA;
    private final DepartmentServiceA departmentServiceA;
    @Autowired
    private AutoArrangeGroup autoArrangeGroup;

    public SportMeetingServiceA(StaffPowerMapper staffPowerMapper, SportMeetingsMapper sportMeetingsMapper, MyInfoMapper myInfoMapper, MyInfoServiceA myInfoServiceA, DepartmentServiceA departmentServiceA, LoginRegisterServiceA loginRegisterServiceA, LoginRegisterMapper loginRegisterMapper, SportMapper sportMapper, AutoArrangeDuration autoArrangeDuration) {
        this.staffPowerMapper = staffPowerMapper;
        this.sportMeetingsMapper = sportMeetingsMapper;
        this.myInfoMapper = myInfoMapper;
        this.myInfoServiceA = myInfoServiceA;
        this.departmentServiceA = departmentServiceA;
        this.loginRegisterServiceA = loginRegisterServiceA;
        this.loginRegisterMapper = loginRegisterMapper;
        this.sportMapper = sportMapper;
    }

    @Override
    @Transactional
    public void joinSportMeeting(Integer smId, String inviteCode) throws UserNamePasswordNoExistException {
        User currentUser = Interceptor.getCurrentUser();
        InviteCode inviteCodeD = staffPowerMapper.getInviteCodeByCode(inviteCode);
        if (inviteCodeD == null) {
            log.info("不要乱搞，这邀请码不能用就是不能用");
            throw new RuntimeException("邀请码异常");
        } else if (!smId.equals(inviteCodeD.getSmId())) {
            log.info("这不是你运动会的邀请码");
            throw new RuntimeException("当前邀请码不是本运动会的邀请码，请核对");
        }
        UserSm userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), inviteCodeD.getSmId());
        if (userSm != null) {
            log.info("请不要重复加入运动会");
            throw new RuntimeException("您已加入当前运动会");
        }
        sportMeetingsMapper.addJoinedSmsByUid(currentUser.getUid(), inviteCodeD.getSmId(), inviteCodeD.getPowerDegree());
        myInfoServiceA.modifyUserSmId(smId);
    }

    @Override
    public void quitSportMeeting(Integer smId) {
        User currentUser = Interceptor.getCurrentUser();
        //....待续

//        List<Integer> joinedSms = myInfoMapper.getUserInfoByUid(currentUser.getUid()).getJoinedSms();
//        joinedSms.remove(smId);
//        sportMeetingsMapper.addJoinedSmsByUid(currentUser.getUid(),joinedSms.toString());
    }

    @Override
    public void updateMeetingInfo(SportMeeting sportMeeting, MultipartFile multipartFile) throws IOException {
        User currentUser = Interceptor.getCurrentUser();
//        log.info(sportMeeting.toString());
        String fileName = departmentServiceA.uploadImg(multipartFile);
        if (fileName != null) {
            sportMeeting.setImg(headPortraitUrl + fileName);
        }
        sportMeeting.setSmId(currentUser.getSmId());
        ZonedDateTime registrationDeadline = sportMeeting.getRegistrationDeadline();
        ZonedDateTime originalDeadline = sportMeetingsMapper.originalDeadline(currentUser.getSmId());
        if(registrationDeadline!=null&&registrationDeadline.compareTo(originalDeadline)<0){
            //如果把截至时间 提前会触发这块
            sportMeetingsMapper.updateAllSportsDeadlineBySmId(sportMeeting.getSmId(), registrationDeadline);
            autoArrangeDuration.arrangeSmDuration(sportMeeting);
            autoArrangeGroup.timer();
        }
        sportMeetingsMapper.updateMeetingInfo(sportMeeting);
    }

    @Override
    public List<SportMeeting> getDemoMeetings() {
        return sportMeetingsMapper.getDomeMeetings();
    }

    @Transactional
    @Override
    public List<Object> addSportMeeting(NewMeeting sportMeeting, MultipartFile multipartFile) throws IOException, UserNamePasswordNoExistException {
        String imgName = departmentServiceA.uploadImg(multipartFile);
        sportMeeting.setImg(headPortraitUrl+ imgName);
        sportMeetingsMapper.addSportMeeting(sportMeeting);

        //初始化运动项目
        if(sportMeeting.getTemplateId()!=null){
            List<Sport> sports = sportMapper.getSports(sportMeeting.getTemplateId());
            Map<Integer,Integer> map=new HashMap<>();//左边原本的rid，右边复制后的rid
            Map<Integer,Integer> mainSpIds=new HashMap<>();//左边原本的，右边复制后的
            for (Sport sport : sports) {
                //处理规则
                Integer rid = map.get(sport.getRid());
                if(rid==null){
                    JudgeRule judgeRule = new JudgeRule();
                    judgeRule.setRid(sport.getRid());
                    sportMeetingsMapper.copyRuleByRid(judgeRule,sportMeeting.getSmId());
                    map.put(sport.getRid(), judgeRule.getRid());
                    rid= judgeRule.getRid();
                }
                sport.setRid(rid);
                //处理mainSpId
                sportMeetingsMapper.copySportsBySport(sport,sportMeeting.getSmId(), ZonedDateTime.now(),sportMeeting.getRegistrationDeadline());
                Integer mainSpId = sport.getMainSpId();
                if(mainSpIds.containsKey(mainSpId)){
                    sport.setMainSpId(mainSpIds.get(mainSpId));
                }else{
                    Integer spId = sport.getSpId();
                    mainSpIds.put(mainSpId, spId);
                    sport.setMainSpId(spId);
                }
                sportMapper.setSportMainSpId(sport);
            }
        }

        //处理创建者
        User currentUser = Interceptor.getCurrentUser();
        currentUser.setSmId(sportMeeting.getSmId());
        currentUser.setPowerDegree("[5]");
        loginRegisterMapper.addUserSmByRegister(currentUser);
        //切换到当前视图
        myInfoMapper.modifySmIdByUid(sportMeeting.getSmId(), currentUser.getUid());
        /*for (User staff : staffs) {
            staff.setSmId(sportMeeting.getSmId());
            staff.setHead(headPortraitUrl+imgName);
            loginRegisterMapper.addUserByRegister(staff);
            loginRegisterMapper.addUserSmByRegister(staff);
        }*/
        return loginRegisterServiceA.login(currentUser.getName(), currentUser.getPassword());
    }

    @Transactional
    @Override
    public void deleteMeeting(){
        Integer smId = Interceptor.getCurrentUser().getSmId();
        sportMeetingsMapper.deleteAppsBySmId(smId);
        sportMeetingsMapper.deleteInviteCodesBySmId(smId);
        sportMeetingsMapper.deleteSchoolsBySmId(smId);
        sportMeetingsMapper.deletePlayersBySmId(smId);
        sportMeetingsMapper.deleteInviteCodesBySmId(smId);
        sportMeetingsMapper.deleteSportsBySmId(smId);
        sportMeetingsMapper.deleteSmsBySmId(smId);
        sportMeetingsMapper.deleteMeeting(smId);
    }

}
