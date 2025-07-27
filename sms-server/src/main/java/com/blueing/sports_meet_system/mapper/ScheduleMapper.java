package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.pojo.SportMeeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    @Select("select sp_id, name, game_start_time, game_end_time, comp_system, comp_type, venue,gender, event_type from sports where sm_id=#{smId} and game_start_time IS NOT NULL")
    List<Sport> getArrangedSports(Integer smId);

    @Select("select sp_id,main_sp_id, name, game_start_time,player_count,count_pgp, game_end_time, comp_system, comp_type, venue, event_type,sub_event_type,size from sports where sm_id=#{smId} and event_type=#{eventType} and game_start_time IS NULL")
    List<Sport> getNoArrangedSports(Integer smId,Integer eventType);

    void modifySportTime(Sport sport);

    @Select("select start_time, end_time, am_start_time, am_end_time, pm_start_time, pm_end_time from sm where sm_id=#{smId}")
    SportMeeting getSportMeetings(Integer smId);

    @Update("update sports set status=3 where status=2 and #{now}> game_start_time")
    void updateSportsStatusTo3(ZonedDateTime now);

}
