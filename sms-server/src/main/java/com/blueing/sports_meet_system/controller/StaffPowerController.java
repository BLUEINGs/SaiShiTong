package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.exception.businessEception.LowPower;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.StaffPowerMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.StaffPowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class StaffPowerController {

    @Autowired
    private StaffPowerService staffPowerService;

    @Autowired
    private StaffPowerMapper staffPowerMapper;

    @GetMapping("/getStaffList")
    public Result<List<User>> getStaffList() {
        User currentUser = Interceptor.getCurrentUser();
        log.info("{}查询了职员列表", currentUser.getName());
        return Result.success(staffPowerService.getStaff());
    }

    @PutMapping("/updateUserPower")
    public Result<Object> updateUserPower(@RequestBody User user) {
        log.info("负责人修改了用户权限");
        staffPowerService.modifyUserSmPowerDegree(user);
        return Result.success(null);
    }

    @PutMapping("/updateInviteCodePower")
    public Result<Object> updateInviteCode(@RequestBody InviteCode inviteCode) {
        log.info("负责人修改了邀请码权限");
        staffPowerService.modifyInviteCode(inviteCode);
        return Result.success(null);
    }

    @GetMapping("/getInviteCodes")
    public Result<List<InviteCode>> getInviteCodes() {
        User currentUser = Interceptor.getCurrentUser();
        log.info("{}查询了邀请码列表", currentUser.getName());
        return Result.success(staffPowerService.getInviteCodes());
    }

    @PostMapping("/createInviteCode")
    public Result<Object> createInviteCode(@RequestBody InviteCode inviteCode) {
        User currentUser = Interceptor.getCurrentUser();
        log.info("{}添加了新的邀请码", currentUser.getName());
        try {
            staffPowerService.addInviteCode(inviteCode);
        } catch (LowPower e) {
            throw new LowPower();
        } catch (Exception e) {
            log.info("当前邀请码重复或者该邀请码的指定对象已被邀请过，且暂未过期");
            List<InviteCode> codes = staffPowerMapper.getInviteCodeByUsers(inviteCode.getUser());
            //只返回权限为一般成员邀请码
            String codeStr = null;
            if (!codes.isEmpty()) {
                for (InviteCode code : codes) {
                    if (code.getUserType().contains(4)||code.getUserType().contains(2)) {
                        codeStr = code.getCode();
                        return new Result<>(0, codeStr, "DUPLICATED_USER");
                    }
                }
            }
            return Result.error(codeStr, "DUPLICATED_CODE");
        }
        return Result.success(null);
    }

    @DeleteMapping("/deleteInviteCode/{code}")
    public Result<Object> deleteInviteCode(@PathVariable("code") String code) {
        log.info("负责人删除了iid为{}的邀请码", code);
        staffPowerMapper.deleteInviteCode(code);
        return Result.success(null);
    }

    @DeleteMapping("/deleteStaff/{uid}")
    public Result<Object> deleteStaff(@PathVariable("uid") Integer uid) {
        log.info("负责人删除了uid为{}的职员", uid);
        User currentUser = Interceptor.getCurrentUser();
        staffPowerMapper.deleteUserSm(uid, currentUser.getSmId());
        return Result.success(null);
    }

}
