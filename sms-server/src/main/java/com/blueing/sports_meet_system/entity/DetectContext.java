package com.blueing.sports_meet_system.entity;

import ai.onnxruntime.OnnxTensor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import java.util.List;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectContext {

    private int[] imgShape;
    private int[] inputShape;
    private int[] offset;

    @Setter
    @Getter
    private float[] inputTensor;

    @Setter
    @Getter
    private OnnxTensor outputOnnxTensor;

    @Setter
    @Getter
    private List<Mat> originMats;

    @Setter
    @Getter
    private List<Mat> lettered;

    @Builder.Default
    private int currentIndex = 0;

    public void setImgShape(int[] imgShape) {
        if (imgShape.length != 2) {
            log.info("赋值失败，图片形状必须为2");
            return;
        }
        this.imgShape = imgShape;
    }

    public void setInputShape(int[] inputShape) {
        if (inputShape.length != 2) {
            log.info("赋值失败，输入张量形状必须为2");
            return;
        }
        this.inputShape = inputShape;
    }

    public void setOffset(int[] offset) {
        if (inputShape.length != 2) {
            log.info("赋值失败，偏移量输入张量形状必须为2");
            return;
        }
        this.offset = offset;
    }

}
