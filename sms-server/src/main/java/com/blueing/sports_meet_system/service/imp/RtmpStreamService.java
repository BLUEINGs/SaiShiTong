/*
* 需求：给我推流地址，我一帧一帧获取，并用cv处理，最后返回每一帧（附带时间戳）的日志，前端拿去画
* 类：onnx会话类，即目标检测类；tracker类，即目标追踪类，负责跟踪逻辑处理并把跟踪结果给tracker类；logger类，即日志记录器，负责处理记录日志；推/拉流类：获取原RTMP流，处理后把json数据按照websocket推给前端
*  */



package com.blueing.sports_meet_system.service.imp;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RtmpStreamService {

    private Thread pullThread;

    private static class StreamWorker implements Runnable{

        private FFmpegFrameGrabber grabber;
        private boolean isRunning=true;

        StreamWorker(String pullUrl){
            grabber=new FFmpegFrameGrabber(pullUrl);
        }

        @Override
        public void run() {
            grabber.setOption("stimeout","5000000");
            try {
                grabber.start();
                while(isRunning){
                    Frame frame = grabber.grab();
                    if (frame==null) break;
                    // 怎么推不上淦

                }
            } catch (FFmpegFrameGrabber.Exception e) {

            }
        }

    }

}
