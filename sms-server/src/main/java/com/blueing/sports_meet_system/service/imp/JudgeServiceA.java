package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.exception.businessEception.BusinessException;
import com.blueing.sports_meet_system.exception.businessEception.LowPower;
import com.blueing.sports_meet_system.exception.businessEception.NotAllScoredException;
import com.blueing.sports_meet_system.interceptor.Interceptor;
import com.blueing.sports_meet_system.mapper.JudgeMapper;
import com.blueing.sports_meet_system.mapper.SportMapper;
import com.blueing.sports_meet_system.pojo.*;
import com.blueing.sports_meet_system.service.JudgeService;
import com.blueing.sports_meet_system.service.auto.AutoRisePlayer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class JudgeServiceA implements JudgeService {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SportMapper sportMapper;
    @Autowired
    private AutoRisePlayer autoRisePlayer;

    @Override
    public List<Judge> getJudges() {
        User currentUser = Interceptor.getCurrentUser();
        return judgeMapper.getJudgesBySmId(currentUser.getSmId());
    }

    @Override
    public void assignJudges(Integer spId, List<Integer> judgeUids) {
        List<Integer> judges0 = judgeMapper.getJudgeUidBySpId(spId);
        List<Integer> allJudges = new ArrayList<>(judges0);

        List<Integer> addList = new ArrayList<>();
        List<Integer> dropList = new ArrayList<>();
        allJudges.addAll(judgeUids);
        for (Integer uid : allJudges) {
            if (!judges0.contains(uid)) {
                addList.add(uid);
                //原本没有而现在有=>新增
            } else if (judges0.contains(uid) && !judgeUids.contains(uid)) {
                dropList.add(uid);
                //原本有而现在没有
            }
        }
        if (!addList.isEmpty()) {
            judgeMapper.addJudges(addList, spId);
        }
        if (!dropList.isEmpty()) {
            judgeMapper.dropJudgeUids(dropList, spId);
        }
    }

    @Override
    public List<Judgement> getJudgements() {
        User currentUser = Interceptor.getCurrentUser();
        if (currentUser.getUserType().contains(5)) {
            return judgeMapper.getJudgementsBySmId(currentUser.getSmId());
        } else {
            return judgeMapper.getJudgementsByUid(currentUser.getSmId(), currentUser.getUid());
        }
    }

    public void saveJudgeRule(JudgeRuleB judgeRule) {
        User currentUser = Interceptor.getCurrentUser();
        if (judgeRule.getRid() == null) {
            judgeMapper.addJudgeRule(judgeRule, currentUser.getSmId());
        } else {
            judgeMapper.modifyJudgeRule(judgeRule);
        }
    }

    public void addPlayerScore(Score score) throws Exception {
        User currentUser = Interceptor.getCurrentUser();
        if (currentUser.getJudgeEvents() == null || !currentUser.getJudgeEvents().contains(score.getSpId())) {
            throw new LowPower();
        }
        if (judgeMapper.getSportBySpId(score.getSpId()).getStatus() != 3) {
            //禁止非评分时段内评分
            log.info("禁止非评分时段内评分");
            throw new BusinessException("禁止咋非评分时段内评分");
        }
        judgeMapper.addAppScore(score);
        log.info("-----------------OK呀现在开始算分------------------");
        List<ApplicationSport> apps = judgeMapper.getAppsBySpId(score.getSpId());
        boolean grantRank = true;
        try {
            apps.sort((o1, o2) -> multiUnitCompare(o1.getScore(), o2.getScore()));
        } catch (NullPointerException e) {
            grantRank = false;
            log.info("排名要在全部赋分后生成");
        }
        try {
            mapperDegrees(apps, grantRank);
        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new NotAllScoredException();
        }
        log.info("当前apps,{}",apps);
        for (ApplicationSport app : apps) {
            log.info("测试一波，{}",app);
            if(app.getARank()==null&&app.getDegree()==null&&app.getScore()==null){
                continue;
            }
            judgeMapper.addRankAndDegree(app);
        }
        log.info("全部弄完了，结果：{}", apps);
        if (grantRank) {
            Sport sport = new Sport();
            sport.setStatus(4);//评分结束这个一定是要拉出来的，这是给自动晋级确认晋级用的
            sport.setSpId(score.getSpId());
            sportMapper.modifySportNoSignGender(sport);
            autoRisePlayer.timer();
        }
//        log.info("现在开始判断是否全部赋分了啊");
    }


    /*
     * 该方法映射成绩->分数，并返回当前评分规则是ASC（>1）还是DESC（<1）
     * */
    private void mapperDegrees(List<ApplicationSport> apps, Boolean grantRank) throws JsonProcessingException {
        JudgeRuleB judgeRule = judgeMapper.getJudgeRuleBySpId(apps.getFirst().getSpId());
        String mappings0 = judgeRule.getMappings();
        Map<String, Integer> map0 = objectMapper.readValue(mappings0, Map.class);
//        log.info("map0的值,{}",map0.values());
        List<Map.Entry<String, Integer>> mappings = new ArrayList<>(map0.entrySet());
        mappings.sort(Comparator.comparingDouble(Map.Entry::getValue));
        Integer order = getOrder(mappings);
        log.info("现在是{}顺序", order);
        //这是排名代码
        if (grantRank) {
            if (order < 0) {
                String lastScore=null;
                for (int i = 0; i < apps.size(); i++) {
                    lastScore = setRankAndGetLastScore(apps, lastScore, i);
                }
            } else {
                String lastScore=null;
                for (int r = apps.size() - 1; r >= 0; r--) {
                    lastScore = setRankAndGetLastScore(apps, lastScore, r);
                }
            }
        }

        //这是赋分代码
//        log.info(apps.toString());
        if (judgeRule.getIsRankMode()) {
            for (ApplicationSport app : apps) {
                if (order > 0) {
                    for (int i = 0; i < mappings.size() - 1; i++) {
                        Map.Entry<String, Integer> mapping = mappings.get(i);
                        if (rankCompareHyr(app.getARank(), mapping.getKey()) > 0
                                && rankCompareHyr(app.getARank(), mappings.get(i + 1).getKey()) <= 0) {
                            app.setDegree(mapping.getValue());
                            break;
                        }
                    }
                    if (app.getDegree() != null) {
                        continue;
                    }
                    if (rankCompareHyr(app.getARank(), mappings.getFirst().getKey()) < 0) {
                        app.setDegree(mappings.getFirst().getValue());
                        //如果比最上面的都大则
                    } else {
                        app.setDegree(mappings.getLast().getValue());
                    }
                } else {
                    log.info("这是个逆序啊");
                    for (int i = mappings.size() - 1; i > 0; i--) {
                        Map.Entry<String, Integer> mapping = mappings.get(i);
                        log.info("当前传入的名次为：{}；映射表的值为:{}",app.getARank(),mapping.getKey());
                        if (rankCompareHyr(app.getARank(), mapping.getKey()) >= 0
                                && rankCompareHyr(app.getARank(), mappings.get(i - 1).getKey()) < 0) {
                            app.setDegree(mapping.getValue());
                            break;
                        }
                    }
                    if (app.getDegree() != null) {
                        continue;
                    }
                    if (rankCompareHyr(app.getARank(), mappings.getLast().getKey()) < 0) {
                        app.setDegree(mappings.getLast().getValue());
                        //如果比最上面的都大则
                    } else {
                        app.setDegree(mappings.getFirst().getValue());
                    }
                }
            }
        } else {
            for (ApplicationSport app : apps) {
                if (order > 0) {
                    for (int i = 0; i < mappings.size() - 1; i++) {
                        Map.Entry<String, Integer> mapping = mappings.get(i);
                        if (app.getScore() == null) {
                            continue;
                        }
                        if (multiUnitCompareHyr(app.getScore(), mapping.getKey()) > 0
                                && multiUnitCompareHyr(app.getScore(), mappings.get(i + 1).getKey()) <= 0) {
                            app.setDegree(mapping.getValue());
                            break;
                        }
                    }
                    if (app.getDegree() != null || app.getScore() == null) {
                        continue;
                    }
                    if (multiUnitCompareHyr(app.getScore(), mappings.getFirst().getKey()) < 0) {
                        app.setDegree(mappings.getFirst().getValue());
                        //如果比最上面的都大则
                    } else {
                        app.setDegree(mappings.getLast().getValue());
                    }
                } else {
                    log.info("这是个逆序啊");
                    for (int i = mappings.size() - 1; i > 0; i--) {
                        Map.Entry<String, Integer> mapping = mappings.get(i);
                        if (app.getScore() == null) {
                            continue;
                        }
                        if (multiUnitCompareHyr(app.getScore(), mapping.getKey()) >= 0
                                && multiUnitCompareHyr(app.getScore(), mappings.get(i - 1).getKey()) < 0) {
                            log.info("当前value" + mapping);
                            app.setDegree(mapping.getValue());
                            break;
                        }
                    }
                    if (app.getDegree() != null || app.getScore() == null) {
                        continue;
                    }
                    if (multiUnitCompareHyr(app.getScore(), mappings.getLast().getKey()) < 0) {
                        app.setDegree(mappings.getLast().getValue());
                        //如果比最上面的都大则
                    } else {
                        app.setDegree(mappings.getFirst().getValue());
                    }
                }

            }
        }
    }

    private String setRankAndGetLastScore(List<ApplicationSport> apps, String lastScore, int r) {
        if(apps.get(r).getScore()!=null&&lastScore!=null&&multiUnitCompare(lastScore,apps.get(r).getScore())==0){
            apps.get(r).setARank(r);
        }else{
            apps.get(r).setARank(r + 1);
        }
        lastScore=apps.get(r).getScore();
        return lastScore;
    }

    private Integer rankCompareHyr(Integer aRank, String key) {
//        log.info("{},{}",aRank,key);
        return multiUnitCompareHyr(aRank + "", key);
    }

    private Integer getOrder(List<Map.Entry<String, Integer>> mappings) {
        Integer order = 1;
        if (mappings.size() > 1) {
            order = multiUnitCompareNoUnit(mappings.get(1).getKey(), mappings.get(0).getKey());
        }
        return order;
    }

    private Integer multiUnitCompare(String n1, String n2) {
        log.info("{} {}",n1,n2);
        Double[] bits1 = Arrays.stream(n1.substring(1, n1.length() - 1).split(", ")).map(s -> Double.parseDouble(s.split("=")[1])).toArray(Double[]::new);
        Double[] bits2 = Arrays.stream(n2.substring(1, n2.length() - 1).split(", ")).map(s -> Double.parseDouble(s.split("=")[1])).toArray(Double[]::new);
        return getResult(bits1, bits2);
    }

    private Integer multiUnitCompareNoUnit(String n1, String n2) {
        Double[] bits1 = Arrays.stream(n1.split(":")).map(Double::parseDouble).toArray(Double[]::new);
        Double[] bits2 = Arrays.stream(n2.split(":")).map(Double::parseDouble).toArray(Double[]::new);
        return getResult(bits1, bits2);
    }

    //混合比较器，左边传来不带单位的表示，右边传来带单位的表示
    private Integer multiUnitCompareHyr(String unit, String noUnit) {
        log.info("{} {}", unit, noUnit);
        if(!unit.startsWith("{")){
            return multiUnitCompareNoUnit(unit,noUnit);
        }
        Double[] bits1 = Arrays.stream(unit.substring(1, unit.length() - 1).split(", ")).map(s -> Double.parseDouble(s.split("=")[1])).toArray(Double[]::new);
        Double[] bits2 = Arrays.stream(noUnit.split(":")).map(Double::parseDouble).toArray(Double[]::new);
        //        log.info("被拿来计算了，左边为{}，右边为{}；结果是：{}",bits1,bits2,result);
        return getResult(bits1, bits2);
    }

    private static Integer getResult(Integer[] bits1, Integer[] bits2) {
        Integer result = bits1[0] - bits2[0];
        for (int i = 1; i < (Math.min(bits1.length, bits2.length)); i++) {
            result = result == 0 ? bits1[i] - bits2[i] : result;
        }
        return result;
    }

    private static Integer getResult(Double[] bits1, Double[] bits2) {
        double result = bits1[0] - bits2[0];
        for (int i = 1; i < (Math.min(bits1.length, bits2.length)); i++) {
            result = result == 0 ? bits1[i] - bits2[i] : result;
        }
        return (int)result;
    }

}
