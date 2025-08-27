package com.blueing.sports_meet_system.service.pusher;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.*;
import org.bytedeco.ffmpeg.global.avcodec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JavaCvFramePusher implements FramePusher {
    @Getter
    private final String pushUrl;  // 推流地址（rtmp://srs-server/live/stream1）
    private final int width;       // 视频宽度（需与帧数据匹配）
    private final int height;      // 视频高度（需与帧数据匹配）
    private final int frameRate;   // 帧率（默认30fps）
    private final int bitRate;     // 视频码率（默认2000kbps）
    private final boolean isLocalFile;  // 新增：标记是否为本地文件录制

    private FFmpegFrameRecorder recorder;  // JavaCV推流器核心对象
    private final BlockingQueue<Frame> frameQueue;  // 帧缓存队列（线程安全）
    private final AtomicBoolean isRunning = new AtomicBoolean(false);  // 运行状态
    private Thread pushThread;  // 推流线程（独立线程处理帧写入）
    private volatile boolean isPushing = false;  // 标记是否正在推送帧（避免并发冲突）

    // 构造函数：初始化推流器参数
    public JavaCvFramePusher(String pushUrl, int width, int height) {
        this(pushUrl, width, height, 30, 2000 * 1000);
    }

    public JavaCvFramePusher(String pushUrl, int width, int height, int frameRate, int bitRate) {
        this.pushUrl = pushUrl;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.bitRate = bitRate;
        this.frameQueue = new LinkedBlockingQueue<>(1024);
        // 判断是否为本地文件（支持 "file://" 前缀或直接路径如 "D:/test.mp4"）
        this.isLocalFile = pushUrl.startsWith("file://") || pushUrl.contains("/") || pushUrl.contains("\\");
    }

    /**
     * 启动推流器（内部调用，初始化FFmpegRecorder并启动线程）
     */
    // 关键修改：初始化FFmpegFrameRecorder时根据类型设置参数
    public void start() {
        if (isRunning.get()) {
            log.warn("推流器已运行：{}", pushUrl);
            return;
        }

        try {
            // 1. 创建FFmpegFrameRecorder（本地文件/RTMP推流共用）
            recorder = new FFmpegFrameRecorder(pushUrl, width, height);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);  // 视频编码：H.264
            recorder.setFrameRate(frameRate);
            recorder.setVideoBitrate(bitRate);
            recorder.setPixelFormat(0);  // 像素格式（与输入Frame匹配）

            // 2. 区分本地文件和RTMP推流的参数配置
            if (isLocalFile) {
                // ===== 本地文件特有配置 =====
                recorder.setFormat(getFormatByExtension(pushUrl));  // 根据文件后缀自动识别格式（如mp4/flv）
                recorder.setVideoOption("preset", "medium");  // 文件录制：平衡速度与画质（直播用ultrafast，文件用medium）
                recorder.setVideoOption("crf", "23");  // CRF码率控制（0-51，越小画质越好，23为默认）
                recorder.setAudioChannels(2);  // 示例：添加音频（如有音频帧需设置，无音频可删除）
                recorder.setSampleRate(44100);  // 音频采样率（如44100Hz）
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // 音频编码：AAC
            } else {
                // ===== RTMP推流特有配置（保留原逻辑）=====
                recorder.setFormat("flv");
                recorder.setVideoOption("preset", "ultrafast");
                recorder.setVideoOption("tune", "zerolatency");
                recorder.setAudioChannels(0);  // 无音频
            }

            // 3. 启动录制/推流
            recorder.start();
            log.info("{}成功：{}（分辨率：{}x{}，格式：{}）",
                    isLocalFile ? "文件录制启动" : "RTMP推流启动",
                    pushUrl, width, height, recorder.getFormat());

            // 4. 启动推流线程（复用原逻辑，无需修改）
            isRunning.set(true);
            pushThread = new Thread(this::pushLoop, "FramePusher-" + pushUrl.hashCode());
            pushThread.start();

        } catch (Exception e) {
            log.error("启动{}失败：{}", isLocalFile ? "文件录制" : "推流", pushUrl, e);
            stop();
            throw new RuntimeException(e);
        }
    }

    // 辅助方法：根据文件路径后缀获取FFmpeg格式（如mp4/flv/avi）
    private String getFormatByExtension(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.')+1);
        switch (extension) {
            case "mp4": return "mp4";
            case "flv": return "flv";
            case "avi": return "avi";
            case "mkv": return "matroska";
            default: return "mp4";  // 默认MP4格式
        }
    }

    /**
     * 推流循环：从队列读取帧并写入FFmpegRecorder
     */
    private void pushLoop() {
        while (isRunning.get()) {
            try {
                Frame frame = frameQueue.poll(100, TimeUnit.MILLISECONDS);  // 超时等待新帧
                if (frame != null) {
                    // 写入帧数据（阻塞操作，直到帧被编码发送）
                    recorder.record(frame);
                    // 释放Frame资源（可选，根据业务层是否复用Frame决定）
                    // Frame.release(frame);
                }
            } catch (InterruptedException e) {
                log.info("推流线程中断：{}", pushUrl);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("推流失败（将尝试重启）：{}", pushUrl, e);
                // 发生错误时尝试重启推流器（可选逻辑，根据稳定性需求）
                restartRecorder();
            }
        }

        // 循环结束：停止推流器
        stop();
    }

    /**
     * 重启FFmpegRecorder（网络断开等异常时调用）
     */
    private void restartRecorder() {
        if (!isRunning.get()) return;

        log.info("尝试重启推流器：{}", pushUrl);
        try {
            // 1. 停止旧的recorder
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            // 2. 重新初始化并启动
            recorder = new FFmpegFrameRecorder(pushUrl, width, height);
            // 重新设置编码参数（与start()中一致）
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("flv");
            recorder.setFrameRate(frameRate);
            recorder.setVideoBitrate(bitRate);
            recorder.start();
            log.info("推流器重启成功：{}", pushUrl);
        } catch (Exception e) {
            log.error("推流器重启失败，将终止：{}", pushUrl, e);
            isRunning.set(false);  // 重启失败时标记为停止
        }
    }

    /**
     * 推送帧列表（添加到队列，非阻塞，返回未入队的帧）
     */
    @Override
    public List<Frame> pushFrames(List<Frame> frames) {
        List<Frame> unPushedFrames = new ArrayList<>();
        if (!isRunning.get()) {
            log.error("推流器未运行，无法推送帧：{}", pushUrl);
            unPushedFrames.addAll(frames);
            return unPushedFrames;
        }

        // 将帧添加到队列（非阻塞，队列满时返回未添加的帧）
        for (Frame frame : frames) {
            if (!frameQueue.offer(frame)) {
                unPushedFrames.add(frame);
                log.warn("帧队列已满，丢弃{}帧：{}", unPushedFrames.size(), pushUrl);
            }
        }
        return unPushedFrames;
    }

    /**
     * 停止推流器（释放资源）
     */
    @Override
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }

        log.info("开始停止推流器：{}", pushUrl);
        // 1. 中断推流线程
        if (pushThread != null && pushThread.isAlive()) {
            pushThread.interrupt();
            try {
                pushThread.join(1000);  // 等待线程退出（最多1秒）
            } catch (InterruptedException e) {
                log.error("等待推流线程退出中断", e);
            }
        }

        // 2. 释放FFmpegRecorder资源
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                log.info("FFmpeg推流器已释放：{}", pushUrl);
            } catch (Exception e) {
                log.error("释放推流器资源失败：{}", pushUrl, e);
            }
            recorder = null;
        }

        // 3. 清空队列并释放未处理的帧
        frameQueue.clear();
        log.info("推流器已完全停止：{}", pushUrl);
    }


    @Override
    public boolean isRunning() {
        return isRunning.get();
    }
}