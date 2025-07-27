package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;
import com.blueing.sports_meet_system.pojo.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LoginRegisterService {

    List<Object> login(String userName, String password) throws UserNamePasswordNoExistException;

    @Transactional
    void register(User user, String inviteCode, String powerDegree) throws Exception;
}
