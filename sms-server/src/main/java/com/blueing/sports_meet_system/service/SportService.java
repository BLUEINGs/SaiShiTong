package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.Group;
import com.blueing.sports_meet_system.pojo.Sport;
import com.blueing.sports_meet_system.pojo.SportGroup;

import java.util.List;

public interface SportService {

    List<Group> getGroups(Integer smId);
    Integer deleteSportGroup(Integer mainSpId);
    void addSportGroup(SportGroup sportGroup);
    void modifySportGroup(SportGroup sportGroup);
    List<SportGroup> getAllSport();

    void signUpSport(Integer scId, Integer spId);

    void cancelSport(Integer scId, Integer spId);
}



