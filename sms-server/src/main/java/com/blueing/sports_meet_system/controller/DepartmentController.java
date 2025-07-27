package com.blueing.sports_meet_system.controller;

import com.blueing.sports_meet_system.exception.businessEception.AlreadyJoined;
import com.blueing.sports_meet_system.exception.businessEception.InviteCodeException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.DepartmentMapper;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.mapper.ScheduleMapper;
import com.blueing.sports_meet_system.mapper.SportMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@CrossOrigin
@RestController
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Value("${oss.head-portrait.path}")
    private String headPortraitPath;

    @Value("${oss.head-portrait.url}")
    private String headPortraitUrl;

    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private MyInfoMapper myInfoMapper;

    @Transactional
    @PostMapping("/addSchool")
    public Result<Object> addSchool(@ModelAttribute School school, MultipartFile imgFile) throws Exception {
        log.info("用户添加了学校");
        departmentService.addSchool(school,imgFile);
        return Result.success(null);
    }

    @PostMapping("/modifySchool")
    public Result<Object> modifySchool(@ModelAttribute School school, MultipartFile imgFile) throws Exception {
        User currentUser = Interceptor.getCurrentUser();
        Integer leader = departmentMapper.getLeader(currentUser.getUid(), school.getScId());
        if(currentUser.getJoinedSchools().contains(school.getScId())&&leader!=null||currentUser.getUserType().contains(5)){
            log.info("用户修改了学校");
            if(imgFile!=null){
                String originalFilename = imgFile.getOriginalFilename();
                String ext= originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString() + ext;
                log.info("创建了新的文件名{}", fileName);
                imgFile.transferTo(new File(headPortraitPath+"/"+ fileName));
                school.setImg(headPortraitUrl+fileName);
            }
            return Result.success(departmentService.modifySchool(school));
        }else{
            return new Result<>(0,"权限不足","POWER_LOW");
        }

    }

    @RequestMapping("/deleteSchool")
    public Result<Object> deleteSchool(School school,Boolean deletePlayers) {
        User currentUser = Interceptor.getCurrentUser();
        if(currentUser.getJoinedSchools().contains(school.getScId())||currentUser.getUserType().contains(5)){
            log.info("用户删除了学校");
            departmentService.deleteSchool(school,deletePlayers);
            return Result.success(null);
        }else{
            return new Result<>(0,"权限不足","POWER_LOW");
        }
    }

    @Transactional
    @RequestMapping("/getSchools")
    public Result<List<School>> getProject(Integer smId) {
        log.info("用户查询了项目");
        List<School> schoolList = departmentService.getSchools(smId);
        return Result.success(schoolList);
    }

    @GetMapping("/getAvailableSports")
    public Result<List<AvailableSport>> getAvailableSports(Integer smId){
        if(smId==null){
            smId = Interceptor.getCurrentUser().getSmId();
        }
        return Result.success(departmentMapper.getAvailableSportList(smId));
    }

    @PostMapping("/addPlayer")
    public Result<Object> addPlayer(@ModelAttribute Player player,MultipartFile headFile) throws IOException {
        User currentUser = Interceptor.getCurrentUser();
        if(currentUser.getJoinedSchools().contains(player.getScId())||currentUser.getUserType().contains(5)){
            log.info("用户添加了运动员");
            return Result.success(departmentService.addPlayer(player,headFile));
        }else{
            return new Result<>(0,"权限不足","POWER_LOW");
        }
    }

    @PostMapping("/addLeader")
    public Result<Object> addLeader(@ModelAttribute Player player,MultipartFile headImg) throws IOException {
        player.setUserType(2);
        return addPlayer(player,headImg);
    }

    @PostMapping("/modifyPlayer")
    public Result<Object> modifyPlayer(@ModelAttribute Player player) {
        User currentUser = Interceptor.getCurrentUser();
//        log.info(currentUser.toString());
        Integer leader = departmentMapper.getLeader(currentUser.getUid(), player.getScId());
        Integer scId = departmentMapper.getPlayerByPid(player.getPid()).getScId();
        if(currentUser.getJoinedSchools()!=null&&(leader!=null&& currentUser.getJoinedSchools().contains(scId)||currentUser.getUserType().contains(5))){
            log.info("用户修改了运动员");
            return Result.success(departmentService.modifyPlayer(player));
        }else{
            return new Result<>(0,"权限不足","POWER_LOW");
        }
    }

    @GetMapping ("/deletePlayer")
    public Result<Object> deletePlayer(Integer pid) {
        User currentUser = Interceptor.getCurrentUser();
        Player player=departmentMapper.getPlayerByPid(pid);
        Integer leader = departmentMapper.getLeader(currentUser.getUid(),player.getScId());
        if(leader!=null&& currentUser.getJoinedSchools().contains(player.getScId())||currentUser.getUserType().contains(5)){
            log.info("用户删除了运动员");
            return Result.success(departmentService.deletePlayer(player));
        }else{
            return new Result<>(0,"权限不足","POWER_LOW");
        }
    }

    @GetMapping("/getSchoolDetail")
    public Result<SchoolDetail> getSchoolDetail(Integer scId){
        log.info("用户查看了学校详情");
        SchoolDetail schoolDetails = departmentService.getSchoolDetails(scId);
//        log.info(schoolDetails.toString());
        return Result.success(schoolDetails);
    }

    @GetMapping("/getDepartmentConfig")
    public Result<Map<String,Object>> getDepartmentConfig(){
        User currentUser = Interceptor.getCurrentUser();
        return Result.success(departmentMapper.getDepartmentConfig(currentUser.getSmId()));
    }

    @PostMapping("/updateDepartmentConfig")
    public Result<Object> updateDepartmentConfig(Integer maxPlayers,Integer maxEvents,String levelConfig){
        User currentUser = Interceptor.getCurrentUser();
        departmentMapper.updateDepartmentConfig(maxPlayers,maxEvents,levelConfig,currentUser.getSmId());
        return Result.success(null);
    }

    @PostMapping("/joinSchool")
    public Result<Object> joinSchool(String inviteCode){
        log.info("用户加入了团体");
        try {
            departmentService.joinSchool(inviteCode);
        }catch (AlreadyJoined e){
            return Result.error("您已经是当前团体的成员了","ALREADY_JOINED");
        }catch (InviteCodeException e){
            return Result.error("无效的邀请码","INVALID_CODE");
        }
        return Result.success(null);
    }

    @PostMapping("/quitSchool")
    public Result<Object> quitSchool(Integer scId){
        log.info("用户退出了团体");
//        departmentService.quitSchool(scId);
        departmentService.quitSchool(scId);
        return Result.success(null);
    }


}
