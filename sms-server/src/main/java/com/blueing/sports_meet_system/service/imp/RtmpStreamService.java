/*
 * 需求：给我推流地址，我一帧一帧获取，并用cv处理，最后返回每一帧（附带时间戳）的日志，前端拿去画
 * 类：onnx会话类，即目标检测类；tracker类，即目标追踪类，负责跟踪逻辑处理并把跟踪结果给tracker类；logger类，即日志记录器，负责处理记录日志；推/拉流类：获取原RTMP流，处理后把json数据按照websocket推给前端
 *  */


package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.entity.GameEvent;
import com.blueing.sports_meet_system.service.pusher.FramePushService;
import com.blueing.sports_meet_system.service.pusher.FramePusher;
import com.blueing.sports_meet_system.service.ws.LogsStreamServer;
import com.blueing.sports_meet_system.service.ws.ScoreUpdateServer;
import com.blueing.sports_meet_system.utils.SpringContextHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RtmpStreamService {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        FFmpegLogCallback.set(); // 启用FFmpeg日志
    }

    public void pullAndPush(String pullUrl, Integer spId) {
        executorService.submit(new StreamWorker(spId, pullUrl));
    }

    private static class StreamWorker implements Runnable {

        private final LogsStreamServer logsStreamServer;
        private final FFmpegFrameGrabber grabber;
        private final BallTrackerService ballTrackerService;
        private final FramePushService pushService;
        private final Integer spId;

        StreamWorker(Integer spId, String pullUrl) {
            this.spId = spId;
            ballTrackerService = SpringContextHolder.getBean(BallTrackerService.class);
            logsStreamServer = SpringContextHolder.getBean(LogsStreamServer.class);
            pushService=SpringContextHolder.getBean(FramePushService.class);
            grabber = new FFmpegFrameGrabber(pullUrl);
            log.info("正在连接RTMP服务器: {}", pullUrl);
        }

        @Override
        public void run() {
            try {
                log.info("开始启动grabber...");
                grabber.start();
                if (log.isInfoEnabled()) {
                    log.info("连接成功！");
                    log.info("视频格式: {}", grabber.getFormat());
                    log.info("视频宽度: {}", grabber.getImageWidth());
                    log.info("视频高度: {}", grabber.getImageHeight());
                    log.info("帧率: {}", grabber.getVideoFrameRate());
                }
                List<Frame> frames = new ArrayList<>();
                Frame frame;
                boolean flag = true;
                while (flag) {
                    frame = grabber.grab();
                    if (frame == null || frame.image == null || frame.imageWidth <= 0 || frame.imageHeight <= 0) {
                        continue;
                    }
                    frames.add(frame.clone());
                    if (frames.size() >= 30) {
                        ArrayList<Frame> willProcessList = new ArrayList<>(frames);
                        // CompletableFuture.runAsync(()->{
                        log.info("已达到固定batch，开始处理");
                        List<Frame> results;
                        try {
                            results = ballTrackerService.processFrameBatch(willProcessList);
                            FramePusher pusher = pushService.getOrCreatePusher("D:/test.flv", 1920, 1080);
                            pusher.pushFrames(results);
                            log.info("处理完成，即将回传");
                        } catch (Exception e) {
                            log.info("推理过程中发生异常：", e);
                            throw new RuntimeException(e);
                        } finally {
                            frames.forEach(Frame::close);
                        }

                        // logsStreamServer.sendToAllClient(spId, gameEvents);
                        // },Executors.newFixedThreadPool(1));
                        frames.clear();
                    }
                }

            } catch (FFmpegFrameGrabber.Exception e) {
                log.info("播放过程中发生错误: {}", e.getMessage());
                // throw new RuntimeException(e);
            } catch (Exception e) {
                log.info("播放过程中发生了未知错误: {}", e.getMessage());
                e.printStackTrace();
                // throw new RuntimeException(e);
            } finally {
                // 6. 清理资源
                try {
                    if (grabber != null) {
                        grabber.stop();
                        grabber.release();
                    }
                    log.info("播放器已关闭，资源已释放");
                } catch (Exception e) {
                    log.info("释放资源时发生错误: {}", e.getMessage());
                }
            }
        }
    }

}
