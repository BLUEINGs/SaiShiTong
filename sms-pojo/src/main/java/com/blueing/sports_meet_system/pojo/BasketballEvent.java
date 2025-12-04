package com.blueing.sports_meet_system.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketballEvent {
    private Integer spId;
    private Integer teId;
    private String name;
    private String rgb;
    private String rtmp;
    private String results;
    private ZonedDateTime scoringTime;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Integer score;
    private Integer type;
    private Integer state;
    List<BasketballEvent> list;

    public void setColorStr(String str){
        this.rgb=str;
    }

    @Override
    public String toString() {
        return "{" +
                "\"list\": " + list + "," +
                "\"spId\": \"" + spId + "\"," +
                "\"teId\": \"" + teId + "\"," +
                "\"name\": \"" + name + "\"," +
                "\"rgb\": \"" + rgb + "\"," +
                "\"scoringTime\": \"" + scoringTime + "\"," +
                "\"startTime\": \"" + startTime + "\"," +
                "\"endTime\": \"" + endTime + "\"," +
                "\"score\": \"" + score + "\"," +
                "\"type\": \"" + type + "\"," +
                "\"state\": \"" + state + "\"" +
                "}";
    }
}
