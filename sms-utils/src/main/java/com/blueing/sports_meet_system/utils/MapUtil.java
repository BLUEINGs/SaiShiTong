package com.blueing.sports_meet_system.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapUtil {

    /*public static List<User> toUserList(String str){
        if(str==null){
            return null;
        }else if("[]".equals(str)|| str.isEmpty()){
            return new ArrayList<>();
        }
        str.split("},")
        return
    }*/

    public static List<Integer> toIntList(String str){
        if(str==null){
            return null;
        }else if("[]".equals(str)|| str.isEmpty()){
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.stream(str.substring(1, str.length() - 1).split(", ")).map(Integer::parseInt).toList());
    }

    public static List<String> toStringList(String str){
        if(str==null){
            return null;
        }else if("[]".equals(str)){
            return new ArrayList<>();
        }
//        System.out.println(str);
        //        System.out.println(list);
        return new ArrayList<>(Arrays.stream(str.substring(1, str.length() - 1).split(",")).map(s -> s.replace("\"","")).toList());
    }

}
