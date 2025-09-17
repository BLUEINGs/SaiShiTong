package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.entity.BallPosition;
import com.blueing.sports_meet_system.entity.BasketPosition;
import com.blueing.sports_meet_system.entity.GameEvent;
import com.blueing.sports_meet_system.entity.PlayerAction;
import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.service.BasketballGameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Component
public class FrameLogger {
    private final int saveInterval = 60; // 保存间隔（秒）
    private final String saveDir = "./game_logs";
    private final List<GameEvent> frameEvents = new LinkedList<>();
    private long lastSaveTime = System.currentTimeMillis();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BasketballGameService basketballGameService;

    @PostConstruct
    public void init() {
        File dir = new File(saveDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("无法创建日志目录: {}", saveDir);
        }
        log.info("日志将保存到: {}", dir.getAbsolutePath());
    }

    private int[] getUpperBodyColor(Mat frame, float[] box) {
        int x1 = (int) box[0];
        int y1 = (int) box[1];
        int x2 = (int) box[2];
        int y2 = (int) box[3];

        // 只取上半身部分
        int upperHeight = (y2 - y1) / 2;
        Rect roi = new Rect(x1, y1, x2 - x1, upperHeight);

        // 确保ROI在图像范围内
        if (roi.x() < 0 || roi.y() < 0 ||
                roi.x() + roi.width() > frame.cols() ||
                roi.y() + roi.height() > frame.rows()) {
            return new int[]{0, 0, 0};
        }

        Mat upperBody = new Mat(frame, roi);
        Scalar meanColor = opencv_core.mean(upperBody);
        upperBody.release();

        return new int[]{
                (int) meanColor.get(2), // R
                (int) meanColor.get(1), // G
                (int) meanColor.get(0) // B
        };
    }

    private GameEvent createEvent(Frame frame, String eventType,
                                  float[] ballBox, float[] rimBox,
                                  float[] holderBox, int[] holderColor,
                                  boolean isShooting, Integer teId, boolean isScored) {
        // 设置球的位置
        BallPosition ballPos = null;
        if (ballBox != null) {
            float centerX = (ballBox[0] + ballBox[2]) / 2;
            float centerY = (ballBox[1] + ballBox[3]) / 2;
            ballPos = new BallPosition(centerX, centerY);
        }

        // 设置篮筐位置
        BasketPosition basketPos = null;
        if (rimBox != null) {
            basketPos = new BasketPosition(
                    (rimBox[0] + rimBox[2]) / 2,
                    (rimBox[1] + rimBox[3]) / 2);
        }

        // 创建PlayerAction
        PlayerAction playerAction = null;
        if (holderBox != null && holderColor != null) {
            playerAction = new PlayerAction(
                    isShooting ? "shooting" : "holding",
                    holderColor,
                    teId,
                    1.0f);
        }

        return new GameEvent(
                frame.timestamp,
                eventType,
                ballPos,
                basketPos,
                playerAction);
    }

    private void saveLogs(Integer spId) {
        if (frameEvents.isEmpty()) {
            return;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            new File(saveDir + "/" + spId).mkdir();
            Path filePath = Paths.get(saveDir + "/" + spId, "game_log_" + timestamp + ".json");

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), frameEvents);

            log.info("日志已保存到: {}", filePath);
            frameEvents.clear();
        } catch (Exception e) {
            log.error("保存日志时出错: {}", e.getMessage());
        }
    }

    private String getColorName(int[] rgbColor) {
        int r = rgbColor[0];
        int g = rgbColor[1];
        int b = rgbColor[2];

        if (r > 200 && g < 100 && b < 100)
            return "红色";
        else if (r < 100 && g > 200 && b < 100)
            return "绿色";
        else if (r < 100 && g < 100 && b > 200)
            return "蓝色";
        else if (r > 200 && g > 200 && b < 100)
            return "黄色";
        else if (r > 200 && g > 200 && b > 200)
            return "白色";
        else if (r < 50 && g < 50 && b < 50)
            return "黑色";
        else if (r < 100 && g < 100 && b < 100)
            return "灰色";
        else
            return String.format("RGB(%d,%d,%d)", r, g, b);
    }

    private String eventToText(GameEvent event) {
        if (("shooting".equals(event.getEventType()) || "holding".equals(event.getEventType())) && event.getPlayerAction() != null) {
            String colorName = getColorName(event.getPlayerAction().getPlayerColor());
            String action = "shooting".equals(event.getPlayerAction().getActionType()) ? "投篮" : "传球";
            return String.format("%s %s队队员正在%s", colorName, event.getPlayerAction().getTeId(), action);
        } else if ("ball_flying".equals(event.getEventType())) {
            return "球在空中飞行";
        } else if ("ball_in".equals(event.getEventType())) {
            return "球进框了！";
        }
        return "";
    }

    public GameEvent logFrame(Integer spId, Frame frame, String eventType,
                              float[] ballBox, float[] rimBox,
                              float[] holderBox, int[] holderColor, Integer teId,
                              boolean isShooting, boolean isScored) {
        GameEvent event = createEvent(frame, eventType, ballBox, rimBox,
                holderBox, holderColor, isShooting, teId, isScored);


        if (frameEvents.isEmpty()) {
            log.info("我是空的");
            frameEvents.add(event);
        }

        //进球判断逻辑
        if (!event.equals(frameEvents.getLast()) && !"unknown".equals(event.getEventType())) {
            String description = eventToText(event);
            // 打印自然语言描述
            if (!description.isEmpty()) {
                log.info(description);
            }
            //宏处理：判断谁把球投进的
            if ("ball_in".equals(event.getEventType())) {
                // 如果球进了
                int count = 0;
                GameEvent lastShooting=null;
                GameEvent lastHolding = null;
                ListIterator<GameEvent> iterator = frameEvents.listIterator(frameEvents.size());
                while (iterator.hasPrevious()) {
                    GameEvent previousEvent = iterator.previous();
                    if ("holding".equals(previousEvent.getEventType())) {
                        lastHolding = previousEvent;
                    } else if ("shooting".equals(previousEvent.getEventType())) {
                        lastShooting = previousEvent;
                        break;
                    }else if("ball_in".equals(previousEvent.getEventType()) && count<5){
                        //如果前三个事件内直接是进球，球不可能自己进，我们认为重复或者球没进
                        return null;
                    }
                    count++;
                }
                //大情况一：如果找到运球人了
                if (lastHolding!=null){
                    //情况一：只找到了上一个运球人
                    if(lastShooting==null){
                        basketballGameService.addfraction(lastHolding.getPlayerAction().getTeId(), 2);
                        //情况二：找到了上一个运球人和上一个投篮人，两人是同一队
                    } else if(lastHolding.getPlayerAction().getTeId()==lastShooting.getPlayerAction().getTeId()){
                        basketballGameService.addfraction(lastHolding.getPlayerAction().getTeId(), 2);
                        //情况三：两人是不同队
                    }else{
                        basketballGameService.addfraction(lastHolding.getPlayerAction().getTeId(), 2);
                    }
                    event.setPlayerAction(lastHolding.getPlayerAction());
                    //TO DO 分数判断、更致信的
                    //大情况二：如果找到投篮人了
                }else if(lastShooting!=null){
                    basketballGameService.addfraction(lastShooting.getPlayerAction().getTeId(), 2);
                    event.setPlayerAction(lastShooting.getPlayerAction());
                }
            }

            frameEvents.add(event);
        } else {
            return null;
        }

        // 检查是否需要保存
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= saveInterval * 1000) {
            saveLogs(spId);
            lastSaveTime = currentTime;
        }
        return event;
    }
}
