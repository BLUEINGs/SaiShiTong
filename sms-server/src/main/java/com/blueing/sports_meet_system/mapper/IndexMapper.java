package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface IndexMapper {

    @Select("select sm_id, name, sport_count, school_count, player_count, img, status from sm where sm_id not in (3,15)")
    List<SportMeeting> getSportMeetings();

    @Select("select sm_id, name, sport_count, school_count, player_count, img, status from sm where sm_id=#{smId}")
    SportMeeting getSportMeetingBySmId(Integer smId);

    @Select("select sm_id from sm")
    List<Integer> getSportMeetingSmIds();

    @Select("select sm_id from sm where status>1")
    List<Integer> getSportOnGoingMeetingSmIds();

    @Select("select sm_id,registration_deadline from sm where #{now}>registration_deadline and status=1")
    List<SportMeeting> getSportMeetingsDeadline(ZonedDateTime now);

    @Select("select sp_id, name,game_start_time, game_end_time,comp_type,venue, event_type, sub_event_type, size, status, gender from sports where sm_id=#{smId} and game_start_time>=#{startTime} and game_end_time<#{endTime}")
    List<Sport> getTodaySchedule(Integer smId,ZonedDateTime startTime,ZonedDateTime endTime);

    @Select("select s.sp_id,s.name as eventName,s.gender,s.comp_type, score, degree, a_rank as `rank` from application_sports `as` left join sports s on `as`.sp_id=s.sp_id where pid=#{pid} and `as`.sm_id=#{smId}")
    List<DetailScore> getScoresByPid(Integer pid, Integer smId);

    List<DetailScore> getScoresByScIds(List<Integer> scIds);

    @Select("select t_rank teamRank, sm_rank meetingRank, total_degree totalScore,p_class as teamName from players where pid=#{pid}")
    MyScore getTotalScoreByPid(Integer pid);

    void rankPlayers(Integer pid,Integer teamRank,Integer meetingRank,Double totalDegree,Integer smId);

    void rankSchools(Integer scId,Integer rank,Double degree);

}
