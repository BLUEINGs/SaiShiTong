package com.blueing.sports_meet_system.service.imp;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RTMP推流服务
 * 提供将视频推送到RTMP服务器的功能
 */
@Slf4j
@Service
public class RtmpStreamer {
    private FFmpegFrameRecorder recorder;
    private FFmpegFrameGrabber grabber;
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private Thread streamingThread;

    static {
        // 设置FFmpeg日志
        try {
            // 启用JavaCV的日志功能
            org.bytedeco.javacv.FFmpegLogCallback.set();
        } catch (Exception e) {
            log.error("设置FFmpeg日志失败", e);
        }
    }

    /**
     * 初始化推流器（用于外部帧推送）
     * 适用于只需要推送外部帧的场景，不需要视频源
     * 
     * @param rtmpUrl      RTMP服务器地址
     * @param width        视频宽度
     * @param height       视频高度
     * @param frameRate    帧率
     * @param videoBitrate 视频比特率
     * @throws Exception 初始化异常
     */
    public void initForFramePushing(String rtmpUrl, int width, int height, int frameRate, Integer videoBitrate)
            throws Exception {
        recorder = new FFmpegFrameRecorder(rtmpUrl, width, height);

        // 设置x264编码器
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(videoBitrate != null ? videoBitrate : 2000000); // 降低比特率到2Mbps

        // 设置x264编码器参数
        recorder.setVideoOption("c:v", "libx264");
        recorder.setVideoOption("preset", "veryfast"); // 比ultrafast稍慢但质量更好
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("x264opts", "bframes=0:force-cfr=1"); // 禁用B帧，强制恒定帧率
        recorder.setVideoOption("thread_type", "frame"); // 帧级线程
        recorder.setVideoOption("crf", "23"); // 恒定质量因子

        // RTMP推流参数
        recorder.setOption("rtmp_live", "live");
        recorder.setOption("stimeout", "5000000");
        recorder.setOption("rtmp_buffer", "5000"); // 减小缓冲，降低延迟
        recorder.setOption("fflags", "nobuffer"); // 禁用缓冲
        recorder.setOption("flush_packets", "1"); // 立即发送数据包
        recorder.setOption("max_delay", "0"); // 最小延迟

        recorder.start();
        log.info("帧推送器初始化完成 - 目标URL: {}", rtmpUrl);
    }

    /**
     * 初始化推流器
     * 
     * @param rtmpUrl      RTMP服务器地址，例如：rtmp://localhost/live/stream
     * @param videoSource  视频源地址，可以是本地文件路径或设备（如：0代表默认摄像头）
     * @param width        推流视频宽度
     * @param height       推流视频高度
     * @param frameRate    推流帧率
     * @param videoBitrate 视频比特率，默认4000000 (4Mbps)
     * @throws Exception 初始化异常
     */
    public void init(String rtmpUrl, String videoSource, int width, int height, int frameRate, Integer videoBitrate)
            throws Exception {
        // 1. 初始化grabber
        grabber = new FFmpegFrameGrabber(videoSource);
        grabber.start();

        // 2. 初始化recorder
        recorder = new FFmpegFrameRecorder(rtmpUrl, width, height);

        // 设置格式
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(videoBitrate != null ? videoBitrate : 4000000);

        // 设置编码器参数
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("crf", "28");

        // 设置音频参数（如果视频源有音频）
        if (grabber.hasAudio()) {
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setAudioBitrate(192000); // 192kbps
        }

        recorder.start();
        log.info("推流器初始化完成 - 目标URL: {}", rtmpUrl);
    }

    /**
     * 开始推流
     * 如果需要循环推流，设置loop为true
     * 
     * @param loop 是否循环推流
     */
    public void startStreaming(boolean loop) {
        if (isStreaming.get()) {
            log.warn("推流已经在进行中");
            return;
        }

        isStreaming.set(true);
        streamingThread = new Thread(() -> {
            try {
                streamLoop(loop);
            } catch (Exception e) {
                log.error("推流过程中发生错误: {}", e.getMessage(), e);
            }
        });
        streamingThread.start();
        log.info("推流开始 - 循环模式: {}", loop);
    }

    private void streamLoop(boolean loop) throws Exception {
        Frame frame;
        while (isStreaming.get()) {
            while ((frame = grabber.grab()) != null && isStreaming.get()) {
                recorder.record(frame);
            }

            if (loop && isStreaming.get()) {
                // 重置grabber到开始位置
                grabber.restart();
                log.info("视频结束，重新开始推流");
            } else {
                break;
            }
        }
    }

    /**
     * 推送单帧
     * 用于自定义推流逻辑，比如处理后的帧
     * 
     * @param frame 要推送的帧
     * @throws Exception 推流异常
     */
    public void pushFrame(Frame frame) throws Exception {
        if (recorder != null && frame != null) {
            recorder.record(frame);
        }
    }

    /**
     * 批量推送帧
     * 用于推送已处理好的帧序列
     * 
     * @param frames 要推送的帧列表
     * @throws Exception 推流异常
     */
    public void pushFrames(List<Frame> frames) throws Exception {
        if (recorder == null) {
            throw new IllegalStateException("推流器尚未初始化，请先调用initForFramePushing方法");
        }

        for (Frame frame : frames) {
            if (frame != null) {
                recorder.record(frame);
                Thread.sleep(30); // 添加适当的延时，模拟实时推流
            }
        }
        log.debug("完成{}帧推送", frames.size());
    }

    /**
     * 停止推流
     */
    public void stopStreaming() {
        if (!isStreaming.get()) {
            return;
        }

        isStreaming.set(false);
        try {
            if (streamingThread != null) {
                streamingThread.join(5000); // 等待推流线程结束
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待推流线程结束时被中断", e);
        }
        log.info("推流已停止");
    }

    /**
     * 释放资源
     * 在不再使用推流器时调用此方法释放资源
     */
    public void release() {
        stopStreaming();
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            if (grabber != null) {
                grabber.stop();
                grabber.release();
            }
        } catch (Exception e) {
            log.error("释放资源时发生错误: {}", e.getMessage(), e);
        }
        log.info("推流器资源已释放");
    }

    /**
     * 检查推流状态
     * 
     * @return 当前是否在推流
     */
    public boolean isStreaming() {
        return isStreaming.get();
    }
}
