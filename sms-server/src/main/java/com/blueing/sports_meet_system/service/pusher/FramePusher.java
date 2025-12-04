package com.blueing.sports_meet_system.service.pusher;

import org.bytedeco.javacv.Frame;
import java.util.List;

public interface FramePusher {
    /**
     * 推送帧列表（阻塞直到所有帧写入完成，或返回未写入的帧）
     * @param frames 待推送的帧列表（JavaCV的Frame对象）
     * @return 未成功推送的帧（如推流器已停止则返回全部）
     */
    List<Frame> pushFrames(List<Frame> frames);

    /**
     * 停止推流（释放资源，终止线程）
     */
    void stop();

    /**
     * 判断推流器是否运行中
     */
    boolean isRunning();

    /**
     * 获取推流地址
     */
    String getPushUrl();
}