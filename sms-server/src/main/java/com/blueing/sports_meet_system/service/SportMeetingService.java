package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.pojo.NewMeeting;
import com.blueing.sports_meet_system.pojo.SportMeeting;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SportMeetingService {
    void joinSportMeeting(Integer smId, String inviteCode) throws UserNamePasswordNoExistException;

    void quitSportMeeting(Integer smId);

    void updateMeetingInfo(SportMeeting sportMeeting, MultipartFile multipartFile) throws IOException;

    List<SportMeeting> getDemoMeetings();

    @Transactional
    List<Object> addSportMeeting(NewMeeting sportMeeting, MultipartFile multipartFile) throws IOException, UserNamePasswordNoExistException;

    @Transactional
    void deleteMeeting();
}
