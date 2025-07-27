package com.blueing.sports_meet_system.service;

import com.blueing.sports_meet_system.exception.businessEception.UserNamePasswordNoExistException;

import java.util.List;

public interface MyInfoService {
    List<Object> modifyUserSmId(Integer smId) throws UserNamePasswordNoExistException;
}
