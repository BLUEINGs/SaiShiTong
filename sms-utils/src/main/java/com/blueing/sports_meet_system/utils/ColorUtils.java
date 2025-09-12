package com.blueing.sports_meet_system.utils;

public class ColorUtils {

    /**
     * 计算两个RGB颜色之间的欧几里得距离
     * @param rgb1 第一个颜色，int数组形式[R,G,B]
     * @param rgb2 第二个颜色，int数组形式[R,G,B]
     * @return 欧几里得距离值，越小表示越相似
     */
    public static double calculateEuclideanDistance(Integer[] rgb1, int[] rgb2) {
        // 验证输入
        if (rgb1 == null || rgb2 == null || rgb1.length != 3 || rgb2.length != 3) {
            throw new IllegalArgumentException("输入必须是长度为3的RGB数组");
        }

        // 计算各分量的平方差之和
        double sum = 0;
        for (int i = 0; i < 3; i++) {
            int diff = rgb1[i] - rgb2[i];
            sum += diff * diff;
        }

        // 返回平方根
        return Math.sqrt(sum);
    }

    public static double calculateEuclideanDistance(int[] rgb1, int[] rgb2) {
        // 验证输入
        if (rgb1 == null || rgb2 == null || rgb1.length != 3 || rgb2.length != 3) {
            throw new IllegalArgumentException("输入必须是长度为3的RGB数组");
        }

        // 计算各分量的平方差之和
        double sum = 0;
        for (int i = 0; i < 3; i++) {
            int diff = rgb1[i] - rgb2[i];
            sum += diff * diff;
        }

        // 返回平方根
        return Math.sqrt(sum);
    }

    /**
     * 计算归一化的相似度分数(0-1之间)
     * @param rgb1 第一个颜色
     * @param rgb2 第二个颜色
     * @return 0-1之间的相似度，1表示完全相同
     */
    public static double calculateNormalizedSimilarity(int[] rgb1, int[] rgb2) {
        double maxDistance = Math.sqrt(3 * 255 * 255); // 最大可能距离
        double distance = calculateEuclideanDistance(rgb1, rgb2);
        return 1 - (distance / maxDistance);
    }

    public static void main(String[] args) {
        // 测试示例
        int[] red = {255, 0, 0};
        int[] lightRed = {255, 50, 50};
        int[] blue = {0, 0, 255};

        System.out.println("红-浅红 欧几里得距离: " + calculateEuclideanDistance(red, lightRed));
        System.out.println("红-浅红 归一化相似度: " + calculateNormalizedSimilarity(red, lightRed));

        System.out.println("红-蓝 欧几里得距离: " + calculateEuclideanDistance(red, blue));
        System.out.println("红-蓝 归一化相似度: " + calculateNormalizedSimilarity(red, blue));
    }
}