package com.blueing.sports_meet_system.utils;

public class MathUtil {

    public static Integer minValue(Integer...x){
        Integer min=x[0];
        for (int i = 0; i < x.length; i++) {
            if(i<min){
                min=x[i];
            }
        }
        return min;
    }

    public static Integer minIndex(Integer...x){
        Integer min=x[0];
        int i;
        for (i = 0; i < x.length; i++) {
            if(i<min){
                min=x[i];
            }
        }
        return i;
    }

}
