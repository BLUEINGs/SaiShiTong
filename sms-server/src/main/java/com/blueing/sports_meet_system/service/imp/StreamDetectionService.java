/*
 * 需求：给我推流地址，我一帧一帧获取，并用cv处理，最后返回每一帧（附带时间戳）的日志，前端拿去画
 * 类：onnx会话类，即目标检测类；tracker类，即目标追踪类，负责跟踪逻辑处理并把跟踪结果给tracker类；logger类，即日志记录器，负责处理记录日志；推/拉流类：获取原RTMP流，处理后把json数据按照websocket推给前端
 *  */


package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.entity.GameEvent;
import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.TeamColor;
import com.blueing.sports_meet_system.service.pusher.FramePushService;
import com.blueing.sports_meet_system.service.pusher.FramePusher;
import com.blueing.sports_meet_system.service.ws.LogsStreamServer;
import com.blueing.sports_meet_system.utils.SpringContextHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class StreamDetectionService {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        FFmpegLogCallback.set(); // 启用FFmpeg日志
    }

    public void pullAndPush(Integer taskType, String pullUrl, Integer spId) {
        executorService.submit(new StreamWorker(taskType, spId, pullUrl));
    }

    private static class StreamWorker implements Runnable {

        private final LogsStreamServer logsStreamServer;
        private final FFmpegFrameGrabber grabber;
        private final BallTrackerService ballTrackerService;
        private final BasketballGameMapper basketballGameMapper;
        private final FramePushService pushService;
        private final Integer spId;
        private final String pullUrl;
        private final List<Frame> bufferedFrames;
        private final Integer taskType;

        StreamWorker(Integer taskType, Integer spId, String pullUrl) {
            this.taskType = taskType;
            this.spId = spId;
            this.pullUrl = pullUrl;
            this.bufferedFrames = new ArrayList<>();
            ballTrackerService = new BallTrackerService(1920,1080);
            logsStreamServer = SpringContextHolder.getBean(LogsStreamServer.class);
            pushService = SpringContextHolder.getBean(FramePushService.class);
            basketballGameMapper = SpringContextHolder.getBean(BasketballGameMapper.class);
            grabber = new FFmpegFrameGrabber(pullUrl);
            log.info("正在连接RTMP服务器: {}", pullUrl);
        }

        @Override
        public void run() {
            log.info("开始启动grabber...");
            try {
                grabber.start();
            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
            log.info("连接成功！");
            log.info("视频格式: {}", grabber.getFormat());
            int width = grabber.getImageWidth();
            ballTrackerService.setWidth(width);
            log.info("视频宽度: {}", width);
            int height = grabber.getImageHeight();
            log.info("视频高度: {}", height);
            ballTrackerService.setHeight(height);
            log.info("帧率: {}", grabber.getVideoFrameRate());

            // 查回队伍颜色
            List<TeamColor> teamColors = basketballGameMapper.queryAiContingent(spId);

            // 构造帧路径
            String resourceLink;
            int index;
            if ((index = pullUrl.lastIndexOf("/")) != -1) {
                resourceLink = pullUrl.substring(index);
            } else if ((index = pullUrl.lastIndexOf("\\")) != -1) {
                resourceLink = pullUrl.substring(index).replace("\\", "/").replace("////", "/").replace("//", "/").replace(".", "");
            } else {
                resourceLink = String.valueOf(new Random().nextInt(999999));
            }
            String pushUrl = null;
            switch (taskType) {
                case 0 -> pushUrl = "rtmp://localhost/live" + resourceLink + "-detect";
                case 3 -> pushUrl = "D:/Users/王士豪/Videos/篮球素材" + resourceLink + "-detect.flv";
            }

            // 构造推流器
            FramePusher pusher = pushService.getOrCreatePusher(pushUrl, width, height);
            basketballGameMapper.setResultLink(pushUrl, spId);
            List<Frame> frames = new ArrayList<>();
            Frame frame;
            Frame lastIFrame = null;
            boolean flag = true;
            while (flag) {
                try {
                    if (lastIFrame != null && lastIFrame.keyFrame) {
                        log.info("I帧起手");
                        pusher.pushFrames(bufferedFrames);
                        bufferedFrames.clear();
                        frames.add(lastIFrame);
                        lastIFrame = null;
                    }
                    frame = grabber.grab();
                    if (frame == null || frame.image == null || frame.imageWidth <= 0 || frame.imageHeight <= 0) {
                        continue;
                    }
                    if (frame.keyFrame) {
                        log.info("I帧来了");
                        lastIFrame = frame.clone();
                        if (!frames.isEmpty()) {
                            Object[] results = ballTrackerService.processFrameBatch(frames, spId, teamColors);
                            bufferedFrames.addAll((List<Frame>) results[0]);
                            logsStreamServer.sendToAllClient(spId, (List<GameEvent>) results[1]);
                            frames.clear();
                        }
                        continue;
                    }
                    frames.add(frame.clone());
                    if (frames.size() >= 30) {
                        Object[] results = ballTrackerService.processFrameBatch(frames, spId, teamColors);
                        if (results != null) {
                            bufferedFrames.addAll((List<Frame>) results[0]);
                            logsStreamServer.sendToAllClient(spId, (List<GameEvent>) results[1]);
                            frames.clear();
                        }
                    }
                } catch (FFmpegFrameGrabber.Exception e) {
                    log.info("播放过程中发生错误: {}", e.getMessage());
                } catch (Exception e) {
                    log.info("播放过程中发生了未知错误: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
            /* finally {
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
            } */
        }

    }

}
