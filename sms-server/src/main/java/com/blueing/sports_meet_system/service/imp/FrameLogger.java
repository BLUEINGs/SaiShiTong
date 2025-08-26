package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.entity.BallPosition;
import com.blueing.sports_meet_system.entity.BasketPosition;
import com.blueing.sports_meet_system.entity.GameEvent;
import com.blueing.sports_meet_system.entity.PlayerAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class FrameLogger {
    private final int saveInterval = 60; // 保存间隔（秒）
    private final String saveDir = "./game_logs";
    private final List<GameEvent> frameEvents = new ArrayList<>();
    private long lastSaveTime = System.currentTimeMillis();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            return new int[] { 0, 0, 0 };
        }

        Mat upperBody = new Mat(frame, roi);
        Scalar meanColor = opencv_core.mean(upperBody);
        upperBody.release();

        return new int[] {
                (int) meanColor.get(2), // R
                (int) meanColor.get(1), // G
                (int) meanColor.get(0) // B
        };
    }

    private GameEvent createEvent(Mat frame, Frame rawFrame,
            float[] ballBox,
            List<float[]> basketBoxes,
            float[] holderBox,
            List<float[]> shootingBoxes) {
        // 设置球的位置
        BallPosition ballPos = null;
        if (ballBox != null) {
            float centerX = (ballBox[0] + ballBox[2]) / 2;
            float centerY = (ballBox[1] + ballBox[3]) / 2;
            ballPos = new BallPosition(centerX, centerY);
        }

        // 设置篮筐位置（如果有多个，取最近的）
        BasketPosition basketPos = null;
        if (!basketBoxes.isEmpty() && ballPos != null) {
            float[] nearestBasket = basketBoxes.get(0);
            double minDist = Double.MAX_VALUE;

            for (float[] box : basketBoxes) {
                float centerX = (box[0] + box[2]) / 2;
                float centerY = (box[1] + box[3]) / 2;
                double dist = Math.pow(centerX - ballPos.getX(), 2) +
                        Math.pow(centerY - ballPos.getY(), 2);
                if (dist < minDist) {
                    minDist = dist;
                    nearestBasket = box;
                }
            }

            basketPos = new BasketPosition(
                    (nearestBasket[0] + nearestBasket[2]) / 2,
                    (nearestBasket[1] + nearestBasket[3]) / 2);
        }

        // 判断事件类型和创建相应的PlayerAction
        PlayerAction playerAction = null;
        String eventType = "ball_flying"; // 默认状态

        if (holderBox != null) {
            // 有持球人，判断是传球还是投篮
            int[] color = getUpperBodyColor(frame, holderBox);
            boolean isShooting = !shootingBoxes.isEmpty();

            playerAction = new PlayerAction(
                    isShooting ? "shooting" : "passing",
                    color,
                    1.0f);
            eventType = "player_action";
        } else if (!basketBoxes.isEmpty() && ballPos != null) {
            // 检查是否进球
            for (float[] basketBox : basketBoxes) {
                if (ballPos.getX() > basketBox[0] && ballPos.getX() < basketBox[2] &&
                        ballPos.getY() > basketBox[1] && ballPos.getY() < basketBox[3]) {
                    eventType = "ball_in";
                    break;
                }
            }
        }

        return new GameEvent(
                rawFrame.timestamp,
                eventType,
                ballPos,
                basketPos,
                playerAction);
    }

    private void saveLogs() {
        if (frameEvents.isEmpty()) {
            return;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            Path filePath = Paths.get(saveDir, "game_log_" + timestamp + ".json");

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
        else if (r < 100 && g < 100 && b < 100)
            return "黑色";
        else
            return String.format("RGB(%d,%d,%d)", r, g, b);
    }

    private String eventToText(GameEvent event) {
        if ("player_action".equals(event.getEventType()) && event.getPlayerAction() != null) {
            String colorName = getColorName(event.getPlayerAction().getPlayerColor());
            String action = "shooting".equals(event.getPlayerAction().getActionType()) ? "投篮" : "传球";
            return String.format("%s队员正在%s", colorName, action);
        } else if ("ball_flying".equals(event.getEventType())) {
            return "球在空中飞行";
        } else if ("ball_in".equals(event.getEventType())) {
            return "球进框了！";
        }
        return "";
    }

    public void logFrame(Mat frame, Frame rawFrame,
            float[] ballBox,
            List<float[]> basketBoxes,
            float[] holderBox,
            List<float[]> shootingBoxes) {
        GameEvent event = createEvent(frame, rawFrame, ballBox, basketBoxes,
                holderBox, shootingBoxes);
        frameEvents.add(event);

        // 打印自然语言描述
        String description = eventToText(event);
        if (!description.isEmpty()) {
            log.info(description);
        }

        // 检查是否需要保存
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= saveInterval * 1000) {
            saveLogs();
            lastSaveTime = currentTime;
        }
    }
}
