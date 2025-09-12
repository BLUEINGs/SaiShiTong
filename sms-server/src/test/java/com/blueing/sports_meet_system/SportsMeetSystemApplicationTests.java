package com.blueing.sports_meet_system;

import ai.onnxruntime.OrtException;
import com.blueing.sports_meet_system.service.imp.BallTrackerService;
import com.blueing.sports_meet_system.service.imp.DetectorServiceA;
import com.blueing.sports_meet_system.service.imp.StreamDetectionService;
import com.blueing.sports_meet_system.service.imp.RtmpStreamer;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacv.Java2DFrameUtils.toFrame;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SportsMeetSystemApplicationTests {

    @Autowired
    private DetectorServiceA detectorServiceA;

    @Autowired
    private BallTrackerService ballTrackerService;

    @Autowired
    private RtmpStreamer rtmpStreamer;
    @Autowired
    private StreamDetectionService streamDetectionService;

    @Test
    public void testPullAndPush(){
        String path = System.getenv("PATH");
        log.info("Java Process PATH:\n{}", path);
        // rtmpStreamService.pullAndPush("rtmp://localhost/live/livestream",3);
    }

    // @Test
    /* void testBallTracking() throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(
                "D:\\Users\\王士豪\\Videos\\篮球素材\\video.mkv")) {
            grabber.start();

            int batchSize = 30; // 每批处理30帧
            List<Frame> frameBatch = new ArrayList<>();
            Frame frame;

            List<GameEvent> gameEvents;
            while ((frame = grabber.grab()) != null) {
                if (frame.image == null) {
                    continue;
                }

                frameBatch.add(frame.clone()); // 需要clone以保留帧
                if (frameBatch.size() >= batchSize) {
                    // 处理一批帧
                    gameEvents = ballTrackerService.processFrameBatch(frameBatch);
                    try {
                        for (GameEvent event : gameEvents) {
                            log.info(event.toString());
                        }
                        // 这里可以添加显示或保存处理后的帧的逻辑
                    } catch (Exception e) {
                        log.error("处理帧批次时发生错误", e);
                    }

                    frameBatch.clear(); // 清空批次，准备下一批
                }
            }

            // 处理最后一批（可能不足30帧）
            if (!frameBatch.isEmpty()) {
                try {
                    gameEvents =ballTrackerService.processFrameBatch(frameBatch);
                } catch (Exception e) {
                    log.error("处理最后一批帧时发生错误", e);
                }
            }
        }
    } */

    private void saveProcessedFrames(List<Mat> mats) throws Exception {
        for (int i = 0; i < mats.size(); i++) {
            Mat mat = mats.get(i);
            Frame frame = toFrame(mat).clone();

            rtmpStreamer.initForFramePushing("rtmp://localhost:1935/live/basketball",640,360,24,600000);
            rtmpStreamer.pushFrame(frame);

            mat.release();
            frame.close();
            // log.info("mat地址:{}", mat.address());
            // opencv_imgcodecs.imwrite("processed_" + i + ".png", mat);
        }
    }

    // @Test
    void onnxLoader() throws OrtException, FrameGrabber.Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(
                "F:\\AiProject\\datasets\\basketball_raw\\video.mkv")) {
            grabber.start();

            // 尝试抓取多帧，有些视频第一帧可能是空的
            List<Frame> frames = new ArrayList<>();
            try (OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat()) {
                for (int i = 0; i < 30; i++) {
                    Frame frame;
                    int attempts = 0;
                    do {
                        frame = grabber.grab();
                        attempts++;
                    } while ((frame == null || frame.image == null) && attempts < 10);

                    if (frame == null || frame.image == null) {
                        throw new RuntimeException("无法获取有效视频帧");
                    }

                    // 检查帧类型
                    if (frame.image != null) {
                        log.info("帧类型: {}, 通道: {}, 宽度: {}, 高度: {},时间戳：{}",
                                frame.image.getClass().getSimpleName(),
                                frame.imageChannels,
                                frame.imageWidth,
                                frame.imageHeight,
                                frame.timestamp);

                        // 转换为Mat并立即克隆
                        frames.add(frame.clone());
                        Mat mat = converter.convert(frame);
                        Mat clonedMat = mat.clone();
                        opencv_imgcodecs.imwrite(i + ".png", clonedMat);

                        // 转回Frame并存储
                        // Frame clonedFrame = converter.convert(clonedMat);
                        // frames.add(clonedFrame);

                        // 释放资源
                        mat.release();
                        clonedMat.release();
                    }
                }
            }

            List<List<float[]>> detects = detectorServiceA.detect(frames);
            for (List<float[]> detect : detects) {
                for (float[] floats : detect) {
                    log.info("检测结果：{}", floats);
                }
            }
        }
    }

}
