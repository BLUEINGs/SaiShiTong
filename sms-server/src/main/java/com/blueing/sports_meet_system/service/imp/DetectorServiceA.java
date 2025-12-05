package com.blueing.sports_meet_system.service.imp;

import ai.onnxruntime.*;
import com.blueing.sports_meet_system.entity.DetectContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.bytedeco.javacv.Frame;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.Arrays;

@Slf4j
@Service
public class DetectorServiceA {

    private OrtEnvironment env; // ONNX环境
    private OrtSession session; // 一个会话有多个线程
    private OrtSession.SessionOptions sessionOptions;

    private String inputName;
    private String outputName;

    @Value("${oss.asset.model.basketballYoloModel}")
    public String modelPath;

    public DetectorServiceA() {
    }

    @PostConstruct // 上面没地方处理，如果初始化失败，检测服务就是启动不了
    public void initOnnxSession() throws OrtException {
        // 创建会话环境
        env = OrtEnvironment.getEnvironment();

        // 创建会话设置
        sessionOptions = new OrtSession.SessionOptions();

        // 尝试使用GPU，如果失败则回退到CPU
        try {
            // 启用CUDA
            sessionOptions.addCUDA(0); // 使用第一个GPU设备
            log.info("成功启用GPU加速");
        } catch (OrtException e) {
            log.warn("GPU初始化失败，将使用CPU模式: {}", e.getMessage());
            // CPU相关设置
            sessionOptions.setInterOpNumThreads(4); // 算子内并行
            sessionOptions.setIntraOpNumThreads(2); // 算子间并行
        }

        sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

        /*
         * 创建会话
         * 会话环境：通过模型和会话设置创建会话的工具
         */

        session = env.createSession(modelPath, sessionOptions);
        NodeInfo inputInfo = session.getInputInfo().values().iterator().next();
        inputName = inputInfo.getName();

        NodeInfo outputInfo = session.getOutputInfo().values().iterator().next();
        outputName = outputInfo.getName();
        log.info("ONNX模型加载成功！模型信息：{}", inputInfo);
    }

    /**
     * 唯一对外封装方法，给入帧序列，将为其执行推理
     * @param frames
     * @return bbox
     */
    public List<List<float[]>> detect(List<Frame> frames) {
        float[] inputTensors = new float[frames.size() * 3 * 640 * 640];
        DetectContext context = DetectContext.builder()
                .inputShape(new int[] { 640, 640 })
                .originMats(new ArrayList<>())
                .build();

        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            preprocess(frame, context);
            float[] inputTensor = context.getInputTensor();
            System.arraycopy(inputTensor, 0, inputTensors,
                    i * 3 * context.getInputShape()[0] * context.getInputShape()[1],
                    3 * context.getInputShape()[0] * context.getInputShape()[1]);
        }
        try(OnnxTensor inputOnnxTensors = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputTensors),
                new long[] { frames.size(), 3, 640, 640 })){
            Map<String, OnnxTensor> inputs = Map.of(inputName, inputOnnxTensors);
            // 开始执行推理
            OrtSession.Result result = session.run(inputs);
            OnnxTensor outputTensor = (OnnxTensor) result.get(outputName).get();
            context.setOutputOnnxTensor(outputTensor);
        }catch (OrtException e){
            e.printStackTrace();
            log.info("当前帧无法处理，跳过");
            return null;
        }
        return postprocess(context);
    }

    private static List<List<float[]>> postprocess(DetectContext context) {
        OnnxTensor outputTensor = context.getOutputOnnxTensor();
        // 1. 获取模型输出形状（shape={nc,9,34000} → 三维数组）
        long[] shapeLong = outputTensor.getInfo().getShape();
        // 转 int 类型（数组索引必须是 int）
        int[] shape = new int[shapeLong.length];
        for (int i = 0; i < shapeLong.length; i++) {
            shape[i] = (int) shapeLong[i]; // 假设 shape 元素都能转 int（模型输出通常不会超过 Integer 范围）
        }

        // 2. 获取一维 float 数组（碾平的向量）
        FloatBuffer floatBuffer = outputTensor.getFloatBuffer();
        if(!outputTensor.isClosed()) outputTensor.close();// 防止内存泄漏
        float[] allFlatArray = new float[floatBuffer.remaining()]; // 创建对应长度的数组
        floatBuffer.get(allFlatArray); // FloatBuffer → float[]

        // 解析 shape：dim0=1（批次）, dim1=9（每个目标的属性数，如 x,y,w,h,conf+4个类别）, dim2=34000（目标数量）
        int dim0 = shape[0]; // 1
        int dim1 = shape[1]; // 9
        int dim2 = shape[2]; // 34000

        // 创建张量结构
        List<List<float[]>> filteredImages = new ArrayList<>();
        ArrayList<ArrayList<float[]>> images = new ArrayList<>();
        for (int n = 0; n < dim0; n++) {
            ArrayList<float[]> boxList = new ArrayList<>();
            images.add(boxList);
            filteredImages.add(new ArrayList<>());
            for (int i = 0; i < dim2; i++) {
                boxList.add(new float[dim1]);
            }
        }

        // 构造张量
        for (int n = 0; n < dim0; n++) {
            List<float[]> boxList = images.get(n);
            float[] flatArray = Arrays.copyOfRange(allFlatArray, n * dim1 * dim2, (n + 1) * dim1 * dim2);
            for (int i = 0; i < dim1; i++) {
                for (int j = 0; j < dim2; j++) {
                    float[] box = boxList.get(j);
                    box[i] = flatArray[i * dim2 + j];
                }
            }
        }

        // 下面是置信度过滤逻辑
        float confThreshold = 0.25f; // 置信度阈值（根据模型调整，建议0.2~0.5）
        // List<float[]> filteredBoxes = new ArrayList<>();
        Iterator<List<float[]>> iterator = filteredImages.iterator();
        for (ArrayList<float[]> boxList : images) {
            List<float[]> filteredBoxes = iterator.next();
            for (float[] box : boxList) {
                // 提取当前box的5个类别置信度（索引4~8），找出最大值
                float maxConf = -1;
                for (int i = 4; i < 9; i++) { // 5个类别：box[4]到box[8]
                    if (box[i] > maxConf) {
                        maxConf = box[i];
                    }
                }
                // 只保留最高置信度 ≥ 阈值的box（过滤背景框）
                if (maxConf >= confThreshold) {
                    filteredBoxes.add(box);
                }
            }
        }

        // log.info("置信度过滤后剩余有效框数量：{}（原始框数量：{}）", filteredBoxes.size(), boxList.size());
        // log.info("获取结果,{}", Arrays.toString(boxList.get(2)));
        List<List<float[]>> result = new ArrayList<>();
        // int count = 0;
        for (List<float[]> filteredBoxes : filteredImages) {
            List<float[]> nmsResults = nms(filteredBoxes, context, 0.5f);
            result.add(nmsResults);
        }

        return result;// 返回：nms后的结果
    }

    private static DetectContext preprocess(Frame frame, DetectContext context) {
        // log.info("frame是null吗？{}",frame==null);
        // log.info("frame的情况：{}",frame.imageWidth);
        // Frame->Mat ，Mat类是OpenCV图像类
        int inputWidth = context.getInputShape()[0];
        int inputHeight = context.getInputShape()[1];
        float[] inputTensor = new float[3 * inputWidth * inputHeight];
        try (OpenCVFrameConverter.ToMat toMat = new OpenCVFrameConverter.ToMat()) {
            Mat srcMat = toMat.convertToMat(frame);
            // log.info("srcMat是null吗？{}",srcMat==null);
            Mat letterboxed = letterbox(srcMat, context);
            // context.getOriginMats().add(srcMat.clone());
            srcMat.release();
            Mat rgbMat = new Mat();
            opencv_imgproc.cvtColor(letterboxed, rgbMat, opencv_imgproc.COLOR_BGR2RGB);
            // opencv_imgcodecs.imwrite("letterboxed_debug.jpg", letterboxed);
            letterboxed.release();
            // 归一化：yolo对RGB三个通道每个通道要的都是[0,1]的值
            Mat normalizedMat = new Mat();
            rgbMat.convertTo(normalizedMat, opencv_core.CV_32F, 1.0 / 255, 0);
            rgbMat.release();

            // 维度调整：HWC -> CHW（正确版本）
            MatVector chwMats = new MatVector(3); // 用 MatVector 替代 Mat[]
            opencv_core.split(normalizedMat, chwMats); // 拆分到 MatVector
            normalizedMat.release();
            // 遍历通道（通过 get() 方法获取单个 Mat）
            int index = 0;
            for (int c = 0; c < 3; c++) {
                Mat channelMat = chwMats.get(c); // 获取第 c 个通道的 Mat（R:0, G:1, B:2）
                FloatBuffer floatBuffer = channelMat.getFloatBuffer();
                float[] channelData = new float[floatBuffer.remaining()]; // 数组长度 = 总像素数（rows × cols）
                floatBuffer.get(channelData); // 将缓冲区数据写入数组
                System.arraycopy(channelData, 0, inputTensor, index, channelData.length);
                index += channelData.length;
                channelMat.release(); // 释放单个通道 Mat
            }
            chwMats.close();
        }
        context.setInputTensor(inputTensor);
        return context;
    }

    /**
     * 该方法通过缩放+填充处理原图像到指定大小，不破坏原图比例
     *
     * @param src：原图像
     * @param detectContext：目标检测上下文
     * @return 处理后的图像
     */
    private static Mat letterbox(Mat src, DetectContext detectContext) {
        int targetWidth = detectContext.getInputShape()[0];
        int targetHeight = detectContext.getInputShape()[1];
        // 计算缩放比例（取宽高缩放比例的最小值，避免图像拉伸）
        int imgWidth = src.cols();
        int imgHeight = src.rows();
        detectContext.setImgShape(new int[] { imgWidth, imgHeight });
        double scale = Math.min((double) targetWidth / imgWidth, (double) targetHeight / imgHeight);
        int newWidth = (int) (imgWidth * scale);
        int newHeight = (int) (imgHeight * scale);

        // 缩放图像
        Mat resized = new Mat();
        opencv_imgproc.resize(src, resized, new Size(newWidth, newHeight), 0, 0, opencv_imgproc.INTER_AREA);

        // 创建目标图像（带黑边填充区域）
        Mat letterbox = new Mat(targetHeight, targetWidth, src.type(), Scalar.all(0)); // Scalar(0,0,0) 黑边

        // 将缩放后的图像复制到目标图像中心（计算填充偏移量）
        int xOffset = (targetWidth - newWidth) / 2;
        int yOffset = (targetHeight - newHeight) / 2;
        // Rect是矩形区域对象
        Rect rect = new Rect(xOffset, yOffset, newWidth, newHeight);
        Mat roi = letterbox.apply(rect); // ROI区域（感兴趣区域）
        detectContext.setOffset(new int[] { xOffset, yOffset });
        /*
         * Mat.apply()方法：给进一个区域（Rect/Range），然后该方法返回一个该区域框出的像素块，这些像素块与被提取Mat中对应区域是相同引用
         */

        resized.copyTo(roi); // 复制缩放图像到 ROI
        // 释放临时Mat
        resized.release();
        roi.release();
        return letterbox;
    }

    /**
     * 预处理box格式：转换坐标(cx,cy,w,h→x1,y1,x2,y2) + 提取最高置信度类别及分数
     *
     * @param originalBoxes 原始box列表（每个元素为 [cx, cy, w, h, cls1_conf, ..., cls5_conf]）
     * @return 预处理后的box列表（每个元素为 [x1, y1, x2, y2, max_conf, class_id]）
     */
    private static List<float[]> preprocessBoxesForNMS(List<float[]> originalBoxes, DetectContext context) {
        List<float[]> processedBoxes = new ArrayList<>();

        // 获取原始图像和模型输入尺寸
        int imgWidth = context.getImgShape()[0]; // 原始图像宽度
        int imgHeight = context.getImgShape()[1]; // 原始图像高度
        int modelWidth = context.getInputShape()[0]; // 模型输入宽度
        int modelHeight = context.getInputShape()[1]; // 模型输入高度

        // 计算缩放比例 (考虑letterbox填充的情况)
        float scale = Math.min((float) modelWidth / imgWidth, (float) modelHeight / imgHeight);
        int scaledWidth = (int) (imgWidth * scale);
        int scaledHeight = (int) (imgHeight * scale);

        // 计算填充区域 (letterbox)
        int padX = (modelWidth - scaledWidth) / 2;
        int padY = (modelHeight - scaledHeight) / 2;

        for (float[] box : originalBoxes) {
            // 1. 转换坐标：cx, cy, w, h → x1, y1, x2, y2（模型输出坐标）
            float cx = box[0];
            float cy = box[1];
            float w = box[2];
            float h = box[3];
            float x1 = cx - w / 2;
            float y1 = cy - h / 2;
            float x2 = cx + w / 2;
            float y2 = cy + h / 2;

            // 2. 将坐标从模型输出空间转换回原始图像空间
            // a. 减去填充区域
            x1 = (x1 - padX) / scale;
            y1 = (y1 - padY) / scale;
            x2 = (x2 - padX) / scale;
            y2 = (y2 - padY) / scale;

            // b. 确保坐标在图像范围内
            x1 = Math.max(0, Math.min(x1, imgWidth));
            y1 = Math.max(0, Math.min(y1, imgHeight));
            x2 = Math.max(0, Math.min(x2, imgWidth));
            y2 = Math.max(0, Math.min(y2, imgHeight));

            // 3. 提取5个类别中的最高置信度及类别ID
            float maxConf = -1;
            int classId = -1;
            for (int i = 0; i < 5; i++) {
                float conf = box[4 + i];
                if (conf > maxConf) {
                    maxConf = conf;
                    classId = i;
                }
            }

            // 4. 构造预处理后的box：[x1, y1, x2, y2, max_conf, class_id]
            processedBoxes.add(new float[] { x1, y1, x2, y2, maxConf, classId });
        }
        return processedBoxes;
    }

    public static List<float[]> nms(List<float[]> boxes, DetectContext context, float iouThreshold) {
        // 【新增】先对原始box进行预处理（坐标转换+提取类别）
        List<float[]> processedBoxes = preprocessBoxesForNMS(boxes, context);

        // 1. 按类别分组（不同类别单独进行NMS，避免跨类别抑制）
        List<List<float[]>> classBoxes = groupByClass(processedBoxes);

        // 2. 对每个类别执行NMS
        List<float[]> result = new ArrayList<>();
        for (List<float[]> classBox : classBoxes) {
            result.addAll(nmsForSingleClass(classBox, iouThreshold));
        }

        return result;
    }

    /**
     * 按类别ID分组
     */
    private static List<List<float[]>> groupByClass(List<float[]> boxes) {
        // 用类别ID作为键（boxes[5] 是 classId），值为该类别所有框
        List<List<float[]>> classBoxes = new ArrayList<>();
        if (boxes.isEmpty())
            return classBoxes;

        // 先按 classId 排序，方便分组
        Collections.sort(boxes, Comparator.comparingInt(box -> (int) box[5]));

        int currentClass = (int) boxes.get(0)[5];
        List<float[]> currentGroup = new ArrayList<>();
        for (float[] box : boxes) {
            int boxClass = (int) box[5];
            if (boxClass == currentClass) {
                currentGroup.add(box);
            } else {
                classBoxes.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentGroup.add(box);
                currentClass = boxClass;
            }
        }
        classBoxes.add(currentGroup); // 添加最后一组
        return classBoxes;
    }

    /**
     * 对单个类别的框执行NMS
     */
    private static List<float[]> nmsForSingleClass(List<float[]> boxes, float iouThreshold) {
        List<float[]> keep = new ArrayList<>();
        if (boxes.isEmpty())
            return keep;

        // 1. 按置信度降序排序（优先保留高分框）
        Collections.sort(boxes, (a, b) -> Float.compare(b[4], a[4])); // 按 score（box[4]）倒序

        // 2. 迭代筛选框
        while (!boxes.isEmpty()) {
            // 取出置信度最高的框（第一个元素）
            float[] maxBox = boxes.get(0);
            keep.add(maxBox); // 保留当前最高置信度框
            boxes.remove(0); // 从列表中移除

            // 遍历剩余框，计算与 maxBox 的 IoU，移除 IoU 超过阈值的框
            List<float[]> newBoxes = new ArrayList<>();
            for (float[] box : boxes) {
                float iou = calculateIoU(maxBox, box);
                if (iou <= iouThreshold) { // IoU 小于阈值，保留
                    newBoxes.add(box);
                }
            }
            boxes = newBoxes; // 更新剩余框列表
        }

        return keep;
    }

    /**
     * 计算两个框的 IoU（交并比）
     *
     * @param box1 框1：[x1, y1, x2, y2, score, classId]
     * @param box2 框2：[x1, y1, x2, y2, score, classId]
     * @return IoU 值（0~1）
     */
    private static float calculateIoU(float[] box1, float[] box2) {
        // 框1坐标
        float x1 = box1[0], y1 = box1[1], x2 = box1[2], y2 = box1[3];
        // 框2坐标
        float a1 = box2[0], b1 = box2[1], a2 = box2[2], b2 = box2[3];

        // 计算交集区域坐标
        float interX1 = Math.max(x1, a1); // 交集左上角x
        float interY1 = Math.max(y1, b1); // 交集左上角y
        float interX2 = Math.min(x2, a2); // 交集右下角x
        float interY2 = Math.min(y2, b2); // 交集右下角y

        // 计算交集面积（若无交集，面积为0）
        float interWidth = Math.max(0, interX2 - interX1);
        float interHeight = Math.max(0, interY2 - interY1);
        float interArea = interWidth * interHeight;

        // 计算两个框的面积
        float area1 = (x2 - x1) * (y2 - y1);
        float area2 = (a2 - a1) * (b2 - b1);

        // 计算并集面积 = 面积1 + 面积2 - 交集面积
        float unionArea = area1 + area2 - interArea;

        // IoU = 交集面积 / 并集面积（避免除零）
        return unionArea == 0 ? 0 : interArea / unionArea;
    }

}
