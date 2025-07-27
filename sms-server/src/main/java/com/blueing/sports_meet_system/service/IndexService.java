package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.DetailScore;
import com.blueing.sports_meet_system.pojo.MyScore;
import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.pojo.SportMeeting;

import java.util.List;
import java.util.Map;

public interface IndexService {

    List<SportMeeting> getSportMeetings();

    Map<String, List<Sport>> getTodaySchedule();

    List<MyScore> getMyScores();

    List<DetailScore> getSchoolScores();
}
