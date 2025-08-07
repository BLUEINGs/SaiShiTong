package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.exception.businessEception.AlreadyJoined;
import com.blueing.sports_meet_system.exception.businessEception.InviteCodeException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.DepartmentMapper;
import com.blueing.sports_meet_system.mapper.LoginRegisterMapper;
import com.blueing.sports_meet_system.mapper.MyInfoMapper;
import com.blueing.sports_meet_system.mapper.StaffPowerMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class DepartmentServiceA implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Value("${oss.head-portrait.path}")
    private String headPortraitPath;

    @Value("${oss.head-portrait.url}")
    private String headPortraitUrl;

    @Autowired
    private StaffPowerMapper staffPowerMapper;

    @Autowired
    private MyInfoMapper myInfoMapper;
    @Autowired
    private LoginRegisterMapper loginRegisterMapper;

    public String uploadImg(MultipartFile imgFile) throws IOException {
        if (imgFile == null) {
            log.info("用户没有上传图片");
            return null;
        }
        String originalFilename = imgFile.getOriginalFilename();
        String ext = null;
        if (originalFilename != null) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + ext;
        log.info("创建了新的文件名{}", fileName);
        imgFile.transferTo(new File(headPortraitPath, fileName));
        return fileName;
    }

    @Override
    public Integer addSchool(School school) {
        return departmentMapper.addSchool(school);
    }

    @Override
    public Integer modifySchool(School school) {
        return departmentMapper.modifySchool(school);
    }

    @Override
    @Transactional
    public void deleteSchool(School school, Boolean isAllDelete) {
        if (isAllDelete) {
            departmentMapper.deleteApplicationsByScId(school.getScId());
            departmentMapper.deletePlayerByScId(school.getScId());
        }
        User currentUser = Interceptor.getCurrentUser();
        deleteUserJoinedSchool(school.getScId(), currentUser);
        departmentMapper.deleteSchool(school);
    }

    @Override
    @Transactional
    public Integer addPlayer(Player player, MultipartFile headFile) throws IOException {
        User currentUser = Interceptor.getCurrentUser();
        player.setSmId(currentUser.getSmId());
        String fileName = uploadImg(headFile);
        player.setHead(headPortraitUrl + fileName);
        if (player.getUserType() != null && player.getUserType() == 2) {
            departmentMapper.addLeader(player);
        } else {
            departmentMapper.addPlayer(player);
        }
        List<ApplicationSport> applist = new ArrayList<>();
        if (player.getSports() != null) {
            for (int i = 0; i < player.getSports().size(); i++) {
                ApplicationSport applicationSport = new ApplicationSport();
                applicationSport.setSpId(player.getSports().get(i));
                applicationSport.setPid(player.getPid());
                applicationSport.setAppTime(ZonedDateTime.now());
                applist.add(applicationSport);
            }
            departmentMapper.addApplications(applist, currentUser.getSmId());
            if (!player.getSports().isEmpty()) {
                departmentMapper.updateSportPlayerCount(player.getSports());
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Integer modifyPlayer(Player player) {
        User currentUser = Interceptor.getCurrentUser();
        int i = departmentMapper.modifyPlayer(player);
        List<Integer> spIds = player.getSports();

        List<Integer> spIds0 = departmentMapper.getSpIdsByPId(player.getScId(), player.getPid());
        Set<Integer> allSpIds = new HashSet<>();
        allSpIds.addAll(spIds0);
        if (spIds != null) {
            allSpIds.addAll(spIds);
        }
        List<ApplicationSport> applicationSports = new ArrayList<>();
//        Map<Integer,Integer> deleteAppMap = new HashMap<>();
        List<PidAndSpId> deleteAppList = new ArrayList<>();
        for (Integer spId : allSpIds) {
            if (!spIds0.contains(spId)) {
                //原本没有就添加
                ApplicationSport app = new ApplicationSport(null, spId, null, ZonedDateTime.now(), null);
                app.setPid(player.getPid());
                applicationSports.add(app);
            }
            if (spIds==null ||!spIds.contains(spId)) {
                //现在没有就删除
                deleteAppList.add(new PidAndSpId(player.getPid(), spId));
            }
        }

        if (!applicationSports.isEmpty()) {
            departmentMapper.addApplications(applicationSports, currentUser.getSmId());
        }
        if (!deleteAppList.isEmpty()) {
//            log.info(deleteAppMap.toString());
            departmentMapper.deleteApplications(deleteAppList);
//            deleteAppMap.entrySet().
        }
        if (!allSpIds.isEmpty()) {
            departmentMapper.updateSportPlayerCount(new ArrayList<>(allSpIds));
        }
        return i;
    }

    @Override
    public Integer deletePlayer(Player player) {
        departmentMapper.deleteApplicationsByPid(player.getPid());
        return departmentMapper.deletePlayer(player);
    }

    @Override
    public List<School> getSchools(Integer smId) {
        if (smId == null) {
            smId = Interceptor.getCurrentUser().getSmId();
        }
        return departmentMapper.getSchoolList(smId);
    }

    @Override
    public SchoolDetail getSchoolDetails(Integer scId) {
        List<Player> playerList = departmentMapper.getPlayerList(scId);
        for (Player player : playerList) {
            player.setSports(departmentMapper.getSpIdsByPId(scId, player.getPid()));
        }
        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setPlayers(playerList);
        List<DetailScore> detailScores = departmentMapper.getDetailScoresByScId(scId);
        List<DetailScore> maleDegreeDetails = new ArrayList<>();
        List<DetailScore> femaleDegreeDetails = new ArrayList<>();
//        List<DetailScore> extraDegreeDetails = new ArrayList<>();
        double maleDegree = 0;
        double femaleDegree = 0;
//        double extraDegree = 0;
        for (DetailScore detailScore : detailScores) {
            if (detailScore.getGender() == null || detailScore.getGender()) {
                maleDegree += detailScore.getDegree();
                maleDegreeDetails.add(detailScore);
            } else {
                femaleDegree += detailScore.getDegree();
                femaleDegreeDetails.add(detailScore);
            }//gender=null直接当男的😋
        }
        schoolDetail.setMaleDegreeDetails(maleDegreeDetails);
        schoolDetail.setFemaleDegreeDetails(femaleDegreeDetails);
//        schoolDetail.setExtraDegreeDetails(extraDegreeDetails);

        schoolDetail.setMaleDegree(maleDegree);
        schoolDetail.setFemaleDegree(femaleDegree);
//        schoolDetail.setExtraDegree(extraDegree);

        schoolDetail.setTotalDegree(maleDegree + femaleDegree);
        return schoolDetail;
    }

    @Override
    public void joinSchool(String code) {
        InviteCode inviteCode = staffPowerMapper.getInviteCodeByCode(code);
        if (inviteCode == null) {
            throw new InviteCodeException();
        }
        User currentUser = Interceptor.getCurrentUser();
        log.info("再一次:{}", currentUser);
        UserSm userSm = myInfoMapper.getUserSmByUidAndSmId(currentUser.getUid(), currentUser.getSmId());

        currentUser.setPowerDegree(userSm.getPowerDegree());
        currentUser.setJoinedSchools(userSm.getJoinedSchools());
        if (inviteCode.getUser() != null) {
            String[] user = inviteCode.getUser().split("=");
            Integer scId = null;
            if ("pid".equals(user[0])) {
                Player player = departmentMapper.getPlayerByPid(Integer.parseInt(user[1]));
                if (player != null) {
                    scId = player.getScId();
                    if (currentUser.getJoinedSchools() != null && currentUser.getJoinedSchools().contains(scId)) {
                        throw new AlreadyJoined();
                    }
                    log.info(player.toString());
                    player.setUid(currentUser.getUid());
                    departmentMapper.modifyPlayer(player);
                } else {
                    throw new InviteCodeException();
                }
                staffPowerMapper.deleteInviteCode(code);
            } else if ("scId".equals(user[0])) {
                scId = Integer.parseInt(user[1]);
                if (currentUser.getJoinedSchools() == null) {
                    currentUser.setJoinedSchools(new ArrayList<>());
                }
                if (currentUser.getJoinedSchools().contains(scId) && departmentMapper.getLeader(currentUser.getUid(), scId) == null) {
                    //如果已经加入学校且不是负责人
                    throw new AlreadyJoined();
                }
                String schoolName = departmentMapper.getSchoolNameByScId(scId).getName();
                if (inviteCode.getUserType().contains(4)) {
                    //若不是负责人就添加为运动员
                    departmentMapper.addPlayer(new Player(null, currentUser.getSmId(), currentUser.getName(), null, null, currentUser.getUid(), null, scId, null, null, schoolName, null, null, null, null,null,null));
                } else {
                    throw new InviteCodeException();
                }
            } else if ("LscId".equals(user[0])) {
                if (inviteCode.getUserType().contains(2)) {
                    //若是负责人就添加为负责人
                    String schoolName = departmentMapper.getSchoolNameByScId(scId).getName();
                    departmentMapper.addLeader(new Player(null, currentUser.getSmId(), currentUser.getName(), null, null, currentUser.getUid(), null, scId, null, null, schoolName, null, null, null, null,null,null));
                }
            }
            School school = new School();
            school.setScId(scId);
            addUserJoinedSchool(school, currentUser);
        } else {
            throw new RuntimeException("该邀请码不能加入团体，请仔细核对");
        }
    }

    @Override
    @Transactional
    public void addSchool(School school, MultipartFile imgFile) throws IOException {
        if (imgFile != null) {
            String fileName = uploadImg(imgFile);
            school.setImg(headPortraitUrl + fileName);
        }
        User currentUser = Interceptor.getCurrentUser();
        school.setSmId(currentUser.getSmId());
        addSchool(school);
//        log.info(school.toString());
        if (currentUser.getUserType().contains(2)) {
            departmentMapper.addLeader(new Player(null, currentUser.getSmId(), currentUser.getName(), null, null, currentUser.getUid(), school.getScId(), null, null, null, school.getName(), null, null, null, null,null,null));
            addUserJoinedSchool(school, currentUser);
        }
    }

    @Override
    public void quitSchool(Integer scId) {
        User currentUser = Interceptor.getCurrentUser();
        try {
            departmentMapper.deletePlayerByUidAndScId(currentUser.getUid(), scId);
        } catch (Exception e) {
            throw new RuntimeException("请先退选参赛，再退出团体");
        }
        deleteUserJoinedSchool(scId, currentUser);
    }

    private void addUserJoinedSchool(School school, User currentUser) {
        List<Integer> joinedSchoolsList = currentUser.getJoinedSchools();
        Set<Integer> joinedSchools;
        if (joinedSchoolsList == null) {
            joinedSchools = new HashSet<>();
        } else {
            joinedSchools = new HashSet<>(joinedSchoolsList);
        }
        //这new HashSet不可能是null都
        joinedSchools.add(school.getScId());
        departmentMapper.updateJoinedSchoolByScId(joinedSchools.toString(), currentUser.getUid(), currentUser.getSmId());
    }

    private void deleteUserJoinedSchool(Integer scId, User currentUser) {
        List<Integer> joinedSchools = currentUser.getJoinedSchools();
        joinedSchools.remove(scId);
        departmentMapper.updateJoinedSchoolByScId(joinedSchools.toString(), currentUser.getUid(), currentUser.getSmId());
    }

}
