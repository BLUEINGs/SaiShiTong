package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.pojo.Player;
import com.blueing.sports_meet_system.pojo.School;
import com.blueing.sports_meet_system.pojo.SchoolDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

//@Slf4j
public interface DepartmentService {
    Integer addSchool(School school);

    Integer modifySchool(School school);

    void deleteSchool(School school, Boolean isAllDelete);

    Integer addPlayer(Player player,MultipartFile multipartFile) throws IOException;

    Integer modifyPlayer(Player player);

    Integer deletePlayer(Player player);

    List<School> getSchools(Integer smId);

    SchoolDetail getSchoolDetails(Integer spId);

    void joinSchool(String code);

    void addSchool(School school, MultipartFile multipartFile) throws IOException;

    void quitSchool(Integer scId);

}
