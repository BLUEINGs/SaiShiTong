package com.blueing.sports_meet_system.mapper;


import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface SportMapper {

    int addSport(Sport sport,Integer smId);

    int modifySport(Sport sport);

    int modifySportNoSignGender(Sport sport);

    int setSportMainSpId(Sport sport);

    @Delete("DELETE FROM sports WHERE main_sp_id = #{mainSpId}")
    int deleteSportGroup(Integer mainSpId);

//    int modifySportGroup(SportGroup sportGroup);

    @Select("select sp_id,rid, name, app_start_time, app_end_time, player_count, game_start_time, game_end_time, comp_system, comp_type, main_sp_id, venue, sm_id, event_type, sub_event_type, size, status, gender,rise_count,rise_type,count_pgp from sports where sm_id = #{smId}")
    List<Sport> getSports(Integer smId);

    @Update("update sports s1 join (SELECT MIN(sp_id) AS min_id FROM sports WHERE main_sp_id = #{mainSpId} AND sp_id != #{spId}) as s2 set app_start_time=game_start_time,app_end_time=game_end_time where s1.sp_id=s2.min_id")
    void updateNextSportStatusBySport(Sport sport);

    Sport getNextSport(Sport sport);

    Sport getLastSport(Sport sport);

    Integer getNextSportSpId(Sport sport);

    @Select("select gid, name, sm_id, sp_id from sp_groups where sp_id =#{spId} ")
    List<Group> getGroups(Integer spId);

    @Select("select a.pid,p.name,p.uid,p.number,p.p_class,a.score,a.degree,p.sc_id from application_sports a left join players p on a.pid=p.pid where gid=#{gid}")
    List<Player> getPlayersByGid(Integer gid);

    @Select("select a.pid,p.name,p.uid,p.number,p.p_class,a.score,a.degree,p.sc_id from application_sports a left join players p on a.pid=p.pid where sp_id=#{spId}")
    List<Player> getPlayersBySpId(Integer spId);

    @Select("select a.pid,p.name,p.uid,p.number,p.p_class,a.score,a.degree,p.sc_id from application_sports a left join players p on a.pid=p.pid where sp_id=#{spId} and gid is null ")
    List<Player> getPlayersBySpIdAndNull(Integer spId);

    @Select("select sp_id,event_type,sub_event_type,size,gender,count_pgp,player_count,sm_id from sports where app_end_time<#{now} and status=1 and count_pgp is not null and player_count is not null and player_count!=0")
    List<Sport> getNoArrangeSports(ZonedDateTime now);

    @Update("update sports set status=2 where count_pgp is null and app_end_time<#{now} and status=1")
    void updateFieldSportStatus(ZonedDateTime now);

    @Select("select a.aid,a.sp_id,a.pid,p.p_class,p.sc_id from application_sports a left join players p on a.pid=p.pid where sp_id=#{spId}")
    List<ApplicationSport> getAppsBySpId(Integer spId);

    @Update("update application_sports set gid=#{gid} where aid=#{aid}")
    void fullAppGid(ApplicationSport applicationSport);

    //该方法返回值为添加进数据库的gid
    int addGroup(Group group);

    //下面的方法是自动晋级用的
    @Select("select sp_id,gender,event_type,sub_event_type,game_start_time,game_end_time,comp_system,comp_type,size,count_pgp,rise_type,rise_count,player_count,sm_id,main_sp_id from sports where game_end_time<#{now} and status=4 and comp_type!=3")
    List<Sport> getNoRisenSports(ZonedDateTime now);

    @Select("select pid,a_rank,gid from application_sports where sp_id=#{spId}")
    List<ApplicationSport> getNoRisenPlayersBySpId(Integer spId);

}


