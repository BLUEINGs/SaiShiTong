package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamColor {

    private Integer teId;
    private String colorStr;
    private Integer[] rgb;

    public void setColorStr(String str){
        this.colorStr=str;
        this.rgb=strToRgb(str);
    }

    public Integer[] strToRgb(String str){
        str = str.replace("(", "").replace(")", "").replace(" ","");
        return Arrays.stream(str.split(",")).map(Integer::parseInt).toArray(_ -> new Integer[3]);
    }

}
