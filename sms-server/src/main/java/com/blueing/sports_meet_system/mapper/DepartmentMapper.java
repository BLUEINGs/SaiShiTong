package com.blueing.sports_meet_system.mapper;

import com.blueing.sports_meet_system.pojo.*;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepartmentMapper {

    @Select("select pid from players where uid=#{uid} and sc_id=#{scId} and user_type=2")
    Integer getLeader(Integer uid,Integer scId);

    @Delete("delete from players where uid=#{uid} and sc_id=#{scId} and user_type=4")
    void deletePlayerByUidAndScId(Integer uid, Integer scId);

    int addSchool(School school);

    @Delete("delete from schools where sc_id=#{scId} ")
    void deleteSchool(School school);

    int modifySchool(School school);

    int addPlayer(Player player);

    int addLeader(Player player);

    @Delete("delete from players where pid=#{pid} ")
    int deletePlayer(Player player);

    int modifyPlayer(Player player);

    int addApplications(List<ApplicationSport> applicationSports, Integer smId);

    int deleteApplications(List<PidAndSpId> pidAndSpIds);

    int deleteApplicationsByScId(Integer scId);

    @Delete("delete from application_sports where pid=(select uid from players where uid=#{uid} and user_type=4)")
    int deleteApplicationsByUid(Integer uid);

    @Delete("delete from application_sports where pid=#{pid}")
    int deleteApplicationsByPid(Integer pid);

    int deletePlayerByScId(Integer scId);

    @Select("select sc_id, name, slogan, img,team_number from schools where sm_id=#{smId}")
    List<School> getSchoolList(Integer smId);

    @Select("select name from schools where sc_id=#{scId}")
    School getSchoolNameByScId(Integer scId);

    @Select("select sp_id,name,gender,comp_system,comp_type,main_sp_id,status from sports where sm_id=#{smId}")
    List<AvailableSport> getAvailableSportList(Integer smId);

    @Select("select a.sp_id from application_sports a left join players p on a.pid=p.pid where p.sc_id=#{scId} and a.pid=#{pid}")
    List<Integer> getSpIdsByPId(Integer scId, Integer pid);

    @Select("select pid, age, gender, p_class, uid, sc_id, number, sports, name,user_type from players where sc_id=#{scId}")
    List<Player> getPlayerList(Integer scId);

    int updateSportPlayerCount(List<Integer> spIds);

    void updateSportPlayerCountByMainSpId(Integer mainSpId);

    @Select("select a.pid,s.sp_id,p.name as athlete_name,a_rank as `rank`,p.gender,s.name as event_name,a.degree,a.score from application_sports a left join players p on a.pid = p.pid left join sports s on s.sp_id=a.sp_id where p.sc_id=#{scId} and score is not null and degree is not null")
    List<DetailScore> getDetailScoresByScId(Integer scId);

    @Update("update user_sm set joined_schools = #{scIds} where uid=#{uid} and sm_id=#{smId}")
    void updateJoinedSchoolByScId(String scIds, Integer uid, Integer smId);

    @Select("select pid,sc_id from players where pid=#{pid}")
    Player getPlayerByPid(Integer pid);

    //接下来是团体页面的配置相关
    @Select("select number_config as levelConfig, max_players maxPlayers, max_events maxEvents from sm where sm_id=#{smId}")
    Map<String, Object> getDepartmentConfig(Integer smId);

    void updateDepartmentConfig(Integer maxPlayers, Integer maxEvents, String levelConfig, Integer smId);

}
