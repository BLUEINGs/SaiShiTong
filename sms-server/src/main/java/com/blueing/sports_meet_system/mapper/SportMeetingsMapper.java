package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.JudgeRule;
import com.blueing.sports_meet_system.pojo.NewMeeting;
import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.pojo.SportMeeting;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface SportMeetingsMapper {

    void addSportMeeting(NewMeeting sportMeeting);
    
    void copySportsBySports(List<Sport> sports,Integer smId,ZonedDateTime now, ZonedDateTime appEndTime);

    void copySportsBySport(Sport s,Integer smId,ZonedDateTime now, ZonedDateTime appEndTime);

    @Insert("insert into user_sm (uid, sm_id, power_degree) value (#{uid},#{smId},#{powerDegree})")
    void addJoinedSmsByUid(Integer uid, Integer smId ,String powerDegree);

    @Select("select sm_id, name,img, sport_count, school_count, player_count, status, start_time, end_time, am_start_time, am_end_time, pm_start_time, pm_end_time,registration_deadline from sm where sm_id=#{smId}")
    SportMeeting getSportMeetingBySmId(Integer smId);

    void updateMeetingInfo(SportMeeting sportMeeting);

    @Select("select registration_deadline from sm where sm_id=#{smId}")
    ZonedDateTime originalDeadline(Integer smId);

    @Update("update sports set app_end_time=#{appEndTime} where sm_id=#{smId} and #{appEndTime}<app_end_time")
    void updateAllSportsDeadlineBySmId(Integer smId,ZonedDateTime appEndTime);

    @Select("select sm_id, name,max_players,max_events, sport_count,number_config, img, status, number_config, max_players, max_events, abstract as description,sm_id, name, sport_count, img, status, number_config, max_players, max_events, abstract, am_start_time, pm_start_time, am_end_time, pm_end_time from demo_sm")
    List<SportMeeting> getDomeMeetings();
    
    void copyRuleByRid(JudgeRule judgeRule,Integer smId);

    @Update("update sm s set player_count=(select count(*) from players p where p.sm_id=s.sm_id), school_count=(select count(*) from schools sc where sc.sm_id=s.sm_id), sport_count =(select count(*) from sports sp where sp.sm_id=s.sm_id)")
    void updateCount();

    @Delete("delete from sm where sm_id=#{smId}")
    void deleteMeeting(Integer smId);

    @Delete("delete from user_sm where sm_id=#{smId}")
    void deleteSmsBySmId(Integer smId);

    @Delete("delete from players where sm_id=#{smId}")
    void deletePlayersBySmId(Integer smId);

    @Delete("delete from application_sports where sm_id=#{smId}")
    void deleteAppsBySmId(Integer smId);

    @Delete("delete from sports where sm_id=#{smId}")
    void deleteSportsBySmId(Integer smId);

    @Delete("delete from schools where sm_id=#{smId}")
    void deleteSchoolsBySmId(Integer smId);

    @Delete("delete from invite_code where sm_id=#{smId}")
    void deleteInviteCodesBySmId(Integer smId);

}
