/*
 * 需求：给我推流地址，我一帧一帧获取，并用cv处理，最后返回每一帧（附带时间戳）的日志，前端拿去画
 * 类：onnx会话类，即目标检测类；tracker类，即目标追踪类，负责跟踪逻辑处理并把跟踪结果给tracker类；logger类，即日志记录器，负责处理记录日志；推/拉流类：获取原RTMP流，处理后把json数据按照websocket推给前端
 *  */


package com.blueing.sports_meet_system.service.imp;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;
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

    public void pull(String pullUrl) {
        executorService.submit(new StreamWorker(pullUrl));
    }

    private static class StreamWorker implements Runnable {

        private final WebSocketServer webSocketServer;
        private final FFmpegFrameGrabber grabber;

        StreamWorker(String pullUrl) {
            webSocketServer = new WebSocketServer();
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
                Frame frame;
                long frameCount = 0;
                long startTime = System.currentTimeMillis();
                while ((frame = grabber.grab()) != null) {
                            frameCount++;
                            if (frameCount % 30 == 0) {
                                long currentTime = System.currentTimeMillis();
                                double elapsedSeconds = (currentTime - startTime) / 1000.0;
                                double fps = frameCount / elapsedSeconds;
                                log.info("已播放: {}秒, 帧数: {}, 实时帧率: {}", elapsedSeconds, frameCount, fps);
                                String json = "OKOKOK";
                                webSocketServer.sendToAllClient(json);
                            }
                }

            } catch (FFmpegFrameGrabber.Exception e) {
                log.info("播放过程中发生错误: {}", e.getMessage());
                throw new RuntimeException(e);
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
