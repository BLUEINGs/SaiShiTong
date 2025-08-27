package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.entity.GameEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<Frame> processFrameBatch(List<Frame> frames) throws Exception {
        List<GameEvent> eventLogs = new ArrayList<>();

        // 1. 批量检测
        List<List<float[]>> batchResults = detectorService.detect(frames);
        if(batchResults==null){
            return null;
        }
        // 2. 处理每一帧
        List<Frame> results=new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            List<float[]> frameResults = batchResults.get(i);

            // 找到当前帧中最可能的球
            float[] ballBox = trackBall(frameResults);

            // 如果找到球，更新位置并绘制
            Mat mat = toMat.convert(frame).clone();

            // 收集其他对象的检测框
            List<float[]> basketBoxes = new ArrayList<>();
            List<float[]> shootingBoxes = new ArrayList<>();
            float[] holderBox = null;

            for (float[] box : frameResults) {
                if ((int) box[5] == 1) { // 篮筐
                    basketBoxes.add(box);
                } else if ((int) box[5] == 2) { // 运动员
                    holderBox = box;
                } else if ((int) box[5] == 4) { // 投篮动作
                    shootingBoxes.add(box);
                }
            }

            // 记录日志
            eventLogs.add(frameLogger.logFrame(mat, frame, ballBox, basketBoxes, holderBox, shootingBoxes));

            //暂时停止绘制图片
            if (ballBox != null) {
                // 绘制边界框
                opencv_imgproc.rectangle(
                        mat,
                        new Rect(
                                (int) ballBox[0], (int) ballBox[1],
                                (int) (ballBox[2] - ballBox[0]),
                                (int) (ballBox[3] - ballBox[1])),
                        new Scalar(0, 255, 0, 0));

                // 更新轨迹
                Point2f center = getBallCenter(ballBox);
                updateTrail(center);

                // 绘制轨迹
                drawTrail(mat);
            }
            // opencv_imgcodecs.imwrite("./test/"+i+".png",mat);
            results.add(toMat.convert(mat).clone());
            // mat.release();
        }

        return results;
    }

    private double calculateDistance(Point2f pos1, Point2f pos2) {
        float dx = pos1.x() - pos2.x();
        float dy = pos1.y() - pos2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private Point2f getBallCenter(float[] box) {
        return new Point2f(
                (box[0] + box[2]) / 2, // x center
                (box[1] + box[3]) / 2 // y center
        );
    }

    private float[] trackBall(List<float[]> results) {
        if (results.isEmpty()) {
            return null;
        }

        List<float[]> balls = new ArrayList<>();
        // 只保留类别为0（篮球）的检测结果
        for (float[] box : results) {
            // box[5]是类别ID
            if ((int) box[5] == 0) { // 假设0是篮球类别
                balls.add(box);
            }
        }

        if (balls.isEmpty()) {
            return null;
        }

        if (balls.size() == 1) {
            lastBallPosition = getBallCenter(balls.get(0));
            return balls.get(0);
        }

        // 如果检测到多个球
        if (lastBallPosition == null) {
            // 没有历史位置，选择置信度最高的
            float[] bestBall = Collections.max(balls,
                    (a, b) -> Float.compare(a[4], b[4])); // box[4]是置信度
            lastBallPosition = getBallCenter(bestBall);
            return bestBall;
        }

        // 有历史位置，选择最近的
        float[] nearestBall = Collections.min(balls,
                (a, b) -> Double.compare(
                        calculateDistance(lastBallPosition, getBallCenter(a)),
                        calculateDistance(lastBallPosition, getBallCenter(b))));

        lastBallPosition = getBallCenter(nearestBall);
        return nearestBall;
    }

    private void updateTrail(Point2f center) {
        double currentTime = (double) opencv_core.getTickCount() / opencv_core.getTickFrequency();

        // 清理过期的轨迹点
        while (!frameTimestamps.isEmpty() &&
                currentTime - frameTimestamps.getFirst() > trailSeconds) {
            ballPositions.removeFirst();
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
                    new Scalar(0, 255, 0, 0));
            prev = curr;
        }
    }
}
