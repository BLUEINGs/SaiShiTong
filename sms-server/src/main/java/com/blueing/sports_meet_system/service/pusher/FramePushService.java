package com.blueing.sports_meet_system.service.pusher;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class FramePushService {
    // 推流器缓存（key：推流地址，value：FramePusher实例）
    private final Map<String, FramePusher> pusherMap = new ConcurrentHashMap<>();
    // 每个推流地址的创建锁（避免并发创建相同推流器）
    private final Map<String, ReentrantLock> pusherLocks = new ConcurrentHashMap<>();

    /**
     * 创建或获取推流器（如已存在则直接返回，否则创建新推流器并启动）
     * @param pushUrl 推流地址（如 rtmp://srs-ip/live/stream1）
     * @param width 视频宽度（需与帧数据匹配）
     * @param height 视频高度（需与帧数据匹配）
     * @return 推流器实例（已在独立线程中启动）
     */
    public FramePusher getOrCreatePusher(String pushUrl, int width, int height) {
        // 1. 检查是否已存在推流器
        FramePusher existingPusher = pusherMap.get(pushUrl);
        if (existingPusher != null && existingPusher.isRunning()) {
            log.debug("复用已有推流器：{}", pushUrl);
            return existingPusher;
        }

        // 2. 加锁创建新推流器（避免并发创建）
        ReentrantLock lock = pusherLocks.computeIfAbsent(pushUrl, k -> new ReentrantLock());
        lock.lock();
        try {
            // 二次检查（防止锁等待期间已有推流器被创建）
            existingPusher = pusherMap.get(pushUrl);
            if (existingPusher != null && existingPusher.isRunning()) {
                return existingPusher;
            }

            // 3. 创建新推流器并启动
            JavaCvFramePusher newPusher = new JavaCvFramePusher(pushUrl, width, height);
            newPusher.start();  // 启动推流器（内部启动独立线程）
            pusherMap.put(pushUrl, newPusher);
            log.info("创建新推流器成功：{}", pushUrl);
            return newPusher;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 推送帧列表到指定地址（通过推流地址找到推流器，调用其pushFrames方法）
     * @param pushUrl 推流地址
     * @param frames 待推送的帧列表（JavaCV的Frame）
     * @return 未成功推送的帧（如推流器不存在或已停止则返回全部）
     */
    public List<Frame> pushFramesToUrl(String pushUrl, List<Frame> frames) {
        FramePusher pusher = pusherMap.get(pushUrl);
        if (pusher == null || !pusher.isRunning()) {
            log.error("推流器不存在或已停止：{}，无法推送帧", pushUrl);
            return frames;  // 返回全部未推送的帧
        }
        log.info("推送成功");
        try {
            return pusher.pushFrames(frames);
        } catch (Exception e) {
            log.error("推送帧到{}失败", pushUrl, e);
            return frames;
        }
    }

    /**
     * 停止指定推流器并从缓存中移除
     * @param pushUrl 推流地址
     */
    public void stopPusher(String pushUrl) {
        FramePusher pusher = pusherMap.remove(pushUrl);
        if (pusher != null) {
            try {
                pusher.stop();
                log.info("已停止并移除推流器：{}", pushUrl);
            } catch (Exception e) {
                log.error("停止推流器失败：{}", pushUrl, e);
            }
        }
        // 移除对应的锁（可选，节省内存）
        pusherLocks.remove(pushUrl);
    }

    /**
     * 停止所有推流器（应用关闭时调用）
     */
    public void stopAllPushers() {
        if (pusherMap.isEmpty()) {
            log.info("无运行中的推流器，无需停止");
            return;
        }

        log.info("开始停止所有推流器（共{}个）", pusherMap.size());
        // 遍历所有推流器，逐个调用stop()
        for (FramePusher pusher : pusherMap.values()) {
            try {
                if (pusher.isRunning()) {
                    pusher.stop();  // 调用推流器的stop()，触发FFmpeg的recorder.stop()，完成文件封装
                    log.info("已停止推流器：{}", pusher.getPushUrl());
                }
            } catch (Exception e) {
                log.error("停止推流器失败：{}", pusher.getPushUrl(), e);
            }
        }
        pusherMap.clear();
        pusherLocks.clear();
        log.info("所有推流器已停止并释放资源");
    }

    @PreDestroy
    public void onDestroy() {
        log.info("===== FramePushService开始销毁，准备停止所有推流器 =====");
        stopAllPushers();  // 调用stopAllPushers()，停止所有推流器
        log.info("===== FramePushService销毁完成 =====");
    }

}