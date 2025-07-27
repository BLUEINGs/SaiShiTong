package com.blueing.sports_meet_system.config;

import com.blueing.sports_meet_system.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private Interceptor interceptor;

    @Autowired
    private DepartmentInterceptor departmentInterceptor;

    @Autowired
    private SportInterceptor sportInterceptor;

    @Autowired
    private JudgeInterceptor judgeInterceptor;

    @Autowired
    private MeetingConfigInterceptor meetingConfigInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/getSportMeetings","/getSportMeeting/**","/login","/register");
        registry.addInterceptor(departmentInterceptor).addPathPatterns("/addSchool")
                .addPathPatterns("/modifySchool")
                .addPathPatterns("/deleteSchool")
                .addPathPatterns("/addPlayer")
                .addPathPatterns("/modifyPlayer")
                .addPathPatterns("/deletePlayer")
                .addPathPatterns("/updateDepartmentConfig")
                .addPathPatterns("/quitSchool")
                .addPathPatterns("/addLeader");
        registry.addInterceptor(sportInterceptor).addPathPatterns("/addSport")
                .addPathPatterns("/modifySport")
                .addPathPatterns("/deleteSport");
        registry.addInterceptor(judgeInterceptor).addPathPatterns("/saveJudgeRule")
                .addPathPatterns("/deleteJudgeRule")
                .addPathPatterns("/updateCompetitionRule")
                .addPathPatterns("/submitScore")
                .addPathPatterns("/getJudgements");
        registry.addInterceptor(meetingConfigInterceptor).addPathPatterns("/updateMeetingTime")
                .addPathPatterns("/updateMeetingBasic")
                .addPathPatterns("/getInviteCodes")
                .addPathPatterns("/createInviteCode")
                .addPathPatterns("/updateInviteCode")
                .addPathPatterns("/deleteStaff/**")
                .addPathPatterns("/updateUserPower")
                .addPathPatterns("/deleteInviteCode/**")
                .addPathPatterns("/deleteMeeting");
    }

}
