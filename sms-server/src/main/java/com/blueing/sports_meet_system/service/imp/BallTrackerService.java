package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.entity.GameEvent;
import com.blueing.sports_meet_system.pojo.TeamColor;
import com.blueing.sports_meet_system.utils.ColorUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Arrays;

@Slf4j
@Service
@Data
public class BallTrackerService {
    private final DetectorServiceA detectorService;
    private final FrameLogger frameLogger;
    private Point2f lastBallPosition; // 上一帧球的位置
    private final int historyFrames = 60; // 历史帧数
    private final float trailSeconds = 3.0f; // 轨迹保留时间
    private final Deque<Point2f> ballPositions; // 球的位置队列
    private final Deque<Double> frameTimestamps; // 时间戳队列
    private final OpenCVFrameConverter.ToMat toMat;

    @Autowired
    public BallTrackerService(DetectorServiceA detectorService, FrameLogger frameLogger) {
        this.detectorService = detectorService;
        this.frameLogger = frameLogger;
        this.ballPositions = new ArrayDeque<>();
        this.frameTimestamps = new ArrayDeque<>();
        this.toMat = new OpenCVFrameConverter.ToMat(); // 该方法底池是一个对象池，对象池有三个Mat对象，转换结果随机覆盖这三个，返回引用只可能是这三个
    }

    private static final float IOU_THRESHOLD = 0.7f; // IoU阈值

    private float calculateIoU(float[] box1, float[] box2) {
        // 计算两个框的交集区域
        float x1 = Math.max(box1[0], box2[0]);
        float y1 = Math.max(box1[1], box2[1]);
        float x2 = Math.min(box1[2], box2[2]);
        float y2 = Math.min(box1[3], box2[3]);

        // 如果没有交集，返回0
        if (x2 < x1 || y2 < y1) {
            return 0.0f;
        }

        // 计算交集面积
        float intersectionArea = (x2 - x1) * (y2 - y1);

        // 计算两个框的面积
        float box1Area = (box1[2] - box1[0]) * (box1[3] - box1[1]);
        float box2Area = (box2[2] - box2[0]) * (box2[3] - box2[1]);

        // 计算并集面积
        float unionArea = box1Area + box2Area - intersectionArea;

        // 返回IoU
        return intersectionArea / unionArea;
    }

    private boolean isContained(float[] box1, float[] box2) {
        return box1[0] >= box2[0] && box1[1] >= box2[1] &&
                box1[2] <= box2[2] && box1[3] <= box2[3];
    }

    private int[] getUpperBodyColor(Mat frame, float[] box) {
        int x1 = (int) box[0];
        int y1 = (int) box[1];
        int x2 = (int) box[2];
        int y2 = (int) box[3];

        // 只取上半身部分
        int upperHeight = (y2 - y1) / 2;
        Rect roi = new Rect(x1, (int) (y1+upperHeight*0.1), x2 - x1, upperHeight);

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

    public Object[] processFrameBatch(List<Frame> frames, Integer spId, List<TeamColor> teamColors) throws Exception {
        List<GameEvent> eventLogs = new ArrayList<>();

        // 1. 批量检测
        List<List<float[]>> batchResults = detectorService.detect(frames);
        if (batchResults == null) {
            return null;
        }
        // 2. 处理每一帧
        List<Frame> results = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            List<float[]> frameResults = batchResults.get(i);

            // 收集各类对象的检测框
            float[] ballBox = null;
            List<float[]> rimBoxes = new ArrayList<>();
            List<float[]> playerBoxes = new ArrayList<>();
            List<float[]> shootingBoxes = new ArrayList<>();
            boolean madeShot = false;

            for (float[] box : frameResults) {
                int category = (int) box[5];
                switch (category) {
                    case 0: // 篮球
                        if (ballBox == null || box[4] > ballBox[4]) {
                            ballBox = box;
                        }
                        break;
                    case 1: // made
                        madeShot = true;
                        break;
                    case 2: // 运动员
                        playerBoxes.add(box);
                        break;
                    case 3: // rim (篮筐)
                        rimBoxes.add(box);
                        break;
                    case 4: // 投篮动作
                        shootingBoxes.add(box);
                        break;
                }
            }

            Mat mat = toMat.convert(frame).clone();

            // 分析当前帧的状态
            String eventType = "unknown";
            float[] holderBox = null;
            int[] holderColor = null;
            boolean isShooting = false;
            boolean isScored = false;

            // 如果有球，进行状态判断
            Integer teId=null;
            if (ballBox != null) {
                // 1. 检查是否进球
                // isScored = madeShot;
                if (madeShot&&!rimBoxes.isEmpty()) {
                    for (float[] rimBox : rimBoxes) {
                        float iou = calculateIoU(ballBox, rimBox);
                        if (iou >= IOU_THRESHOLD || isContained(ballBox, rimBox)) {
                            isScored = true;
                            break;
                        }
                    }
                }

                // 2. 如果没进球，检查是谁在持球
                if (!isScored) {
                    float maxIoU = 0;
                    // 检查与运动员的IoU
                    for (float[] playerBox : playerBoxes) {
                        float iou = calculateIoU(ballBox, playerBox);
                        if (iou > maxIoU) {
                            maxIoU = iou;
                            holderBox = playerBox;
                            isShooting = false;
                        }
                    }
                    // 检查与投篮动作的IoU
                    for (float[] shootBox : shootingBoxes) {
                        float iou = calculateIoU(ballBox, shootBox);
                        if (iou > maxIoU) {
                            maxIoU = iou;
                            holderBox = shootBox;
                            isShooting = true;
                        }
                    }

                    // 如果有持球人，获取其上半身颜色
                    if (holderBox != null) {
                        holderColor = getUpperBodyColor(mat, holderBox);
                        TeamColor teamColor1 = teamColors.get(0);
                        TeamColor teamColor2 = teamColors.get(1);
                        if(ColorUtils.calculateEuclideanDistance(teamColor1.getRgb(),holderColor)<ColorUtils.calculateEuclideanDistance(teamColor2.getRgb(),holderColor)){
                            teId=teamColor1.getTeId();
                        }else {
                            teId=teamColor2.getTeId();
                        }
                    }
                }
            }

            // 根据状态设置事件类型
            if (isScored) {
                eventType = "ball_in";
            } else if (holderBox != null) {
                // log.info("这一帧就是在持球");
                eventType = isShooting ? "shooting" : "holding";
            } else if (ballBox != null) {
                eventType = "ball_flying";
            }

            // 记录日志
            GameEvent event = frameLogger.logFrame(spId, frame, eventType, ballBox,
                    !rimBoxes.isEmpty() ? rimBoxes.get(0) : null,
                    holderBox, holderColor, teId,isShooting, isScored);
            if(event!=null){
                eventLogs.add(event);
            }

            // 绘制跟踪结果
            // 1. 绘制球的边界框和轨迹
            if (ballBox != null) {
                // 绘制球的边界框（绿色）
                Rect ballRect = new Rect(
                        new Point((int) ballBox[0], (int) ballBox[1]),
                        new Point((int) ballBox[2], (int) ballBox[3]));
                opencv_imgproc.rectangle(mat, ballRect, new Scalar(0, 255, 0, 0), 2, opencv_imgproc.LINE_AA, 0);
                // i参数是粗细，i1是线条类型，LINE_AA是抗锯齿，i2是位移，此处不需要

                // 更新并绘制轨迹
                Point2f center = getBallCenter(ballBox);
                updateTrail(center);
                drawTrail(mat);
            }

            // 2. 绘制持球人的边界框（蓝色）
            if (holderBox != null) {
                Rect holderRect = new Rect(
                        new Point((int) holderBox[0], (int) holderBox[1]),
                        new Point((int) holderBox[2], (int) holderBox[3]));
                opencv_imgproc.rectangle(mat, holderRect, new Scalar(255, 0, 0, 0), 1, opencv_imgproc.LINE_AA, 0);
            }

            // 3. 如果有made，绘制made的边界框（黄色）
            if (madeShot) {
                for (float[] box : frameResults) {
                    if ((int) box[5] == 1) { // made类别
                        Rect madeRect = new Rect(
                                new Point((int) box[0], (int) box[1]),
                                new Point((int) box[2], (int) box[3]));
                        opencv_imgproc.rectangle(mat, madeRect, new Scalar(0, 255, 255, 0), 2, opencv_imgproc.LINE_AA, 0);
                        break; // 只画第一个made框
                    }
                }
            }

            results.add(toMat.convert(mat).clone());
            mat.release();
        }

        return new Object[]{results, eventLogs};
    }

    private Point2f getBallCenter(float[] box) {
        return new Point2f(
                (box[0] + box[2]) / 2, // x center
                (box[1] + box[3]) / 2 // y center
        );
    }

    // 移除了trackBall方法，因为我们现在使用IoU来判断球的状态

    private void updateTrail(Point2f center) {
        double currentTime = (double) opencv_core.getTickCount() / opencv_core.getTickFrequency();

        // 清理过期的轨迹点
        while (!frameTimestamps.isEmpty() &&
                currentTime - frameTimestamps.getFirst() > trailSeconds) {
            if(!ballPositions.isEmpty()){
                ballPositions.removeFirst();
            }
            frameTimestamps.removeFirst();
        }

        // 添加新的轨迹点
        ballPositions.addLast(center);
        frameTimestamps.addLast(currentTime);
    }

    private void drawTrail(Mat frame) {
        if (ballPositions.size() <= 1) {
            return;
        }

        Iterator<Point2f> it = ballPositions.iterator();
        Point2f prev = it.next();
        while (it.hasNext()) {
            Point2f curr = it.next();
            opencv_imgproc.line(
                    frame,
                    new Point((int) prev.x(), (int) prev.y()),
                    new Point((int) curr.x(), (int) curr.y()),
                    new Scalar(0, 255, 0, 0), 2, opencv_imgproc.LINE_AA, 0);
            prev = curr;
        }
    }
}
