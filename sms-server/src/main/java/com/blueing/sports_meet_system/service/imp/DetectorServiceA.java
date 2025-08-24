package com.blueing.sports_meet_system.service.imp;

import ai.onnxruntime.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Service;
import org.bytedeco.javacv.Frame;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DetectorServiceA {

    private OrtEnvironment env; // ONNX环境
    private OrtSession session; // 一个会话有多个线程
    private OrtSession.SessionOptions sessionOptions;

    private String inputName;
    private long[] inputShape;
    private List<String> outputNames;

    @PostConstruct  //上面没地方处理，如果初始化失败，检测服务就是启动不了
    public void initOnnxSession() throws OrtException{
        //创建会话环境
        env=OrtEnvironment.getEnvironment();

        // 创建会话设置
        sessionOptions=new OrtSession.SessionOptions();
        sessionOptions.setInterOpNumThreads(4); //算子内并行
        sessionOptions.setInterOpNumThreads(2); //算子间并行
        sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

        /*创建会话
        * 会话环境：通过模型和会话设置创建会话的工具
        *  */
        String modelPath= "F:\\AiProject\\ultralytics-main\\model_test\\detect_service\\best.onnx";
        session=env.createSession(modelPath,sessionOptions);
        NodeInfo inputInfo = session.getInputInfo().values().iterator().next();
        inputName=inputInfo.getName();
        inputShape=((TensorInfo)inputInfo.getInfo()).getShape();
        log.info("ONNX模型加载成功！模型信息：{}",inputInfo);
    }

    private static float[] preprocess(Frame frame,int inputWidth,int inputHeight){
        // Frame->Mat ，Mat类是OpenCV图像类
        // Mat srcMat=
        return null;
    }

}
