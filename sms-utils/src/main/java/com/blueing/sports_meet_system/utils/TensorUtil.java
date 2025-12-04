package com.blueing.sports_meet_system.utils;

import java.util.ArrayList;
import java.util.List;

public class TensorUtil {

    /**
     * 将一维向量转换为多维嵌套ArrayList结构
     * @param vector 输入的一维向量
     * @param shape 目标形状的维度数组
     * @return 嵌套的ArrayList结构表示的多维数据
     */
    public static List<?> convertVectorToMultiDim(float[] vector, int[] shape) {
        if (vector == null || shape == null || shape.length == 0) {
            throw new IllegalArgumentException("输入参数不能为空");
        }

        // 检查向量长度是否与形状匹配
        int totalSize = 1;
        for (int dim : shape) {
            totalSize *= dim;
        }
        if (totalSize != vector.length) {
            throw new IllegalArgumentException("向量长度与形状不匹配");
        }

        return buildNestedList(vector, shape, 0, 0);
    }

    private static List<?> buildNestedList(float[] vector, int[] shape, int dimIndex, int vectorIndex) {
        if (dimIndex == shape.length - 1) {
            // 最内层，创建float数组
            List<Float> leafList = new ArrayList<>(shape[dimIndex]);
            for (int i = 0; i < shape[dimIndex]; i++) {
                leafList.add(vector[vectorIndex + i]);
            }
            return leafList;
        } else {
            // 中间层，创建嵌套的ArrayList
            List<List<?>> nestedList = new ArrayList<>(shape[dimIndex]);
            int elementsPerChild = 1;
            for (int i = dimIndex + 1; i < shape.length; i++) {
                elementsPerChild *= shape[i];
            }

            for (int i = 0; i < shape[dimIndex]; i++) {
                List<?> child = buildNestedList(vector, shape, dimIndex + 1, vectorIndex + i * elementsPerChild);
                nestedList.add((List<?>) child);
            }
            return nestedList;
        }
    }

    /* private static OnnxTensor vectorToOnnxTensor(float[] vector,int[] shape,int i){

        return null;
    };

    //vector n.向量
    public static OnnxTensor vectorToOnnxTensor(float[] vector,int[] shape){
        int dim0 = shape[0]; // 1
        int dim1 = shape[1]; // 9
        int dim2 = shape[2]; // 34000

        // 由于 dim0=1（只有一个批次），直接从第 0 个批次开始处理
        int batchStart = 0; // 批次起始索引（第 0 个批次）
        ArrayList<float[]> boxList = new ArrayList<>();
        for (int i = 0; i < dim2; i++) {
            boxList.add(new float[dim1]);
        }
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                float[] box = boxList.get(j);
                box[i] = vector[i * dim2 + j];
            }
        }
    } */

}
