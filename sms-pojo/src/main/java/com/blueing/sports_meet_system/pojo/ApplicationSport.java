package com.blueing.sports_meet_system.pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSport extends Player {
    private Integer aid;
    private Integer spId;
    private Integer gid;
    private ZonedDateTime appTime;
    private Integer aRank;

    @Override
    public String toString() {
        return "ApplicationSport{" +
                "aid=" + aid +
                ", spId=" + spId +
                ", gid=" + gid +
                ", degree=" + getDegree() +
                ", appTime=" + appTime +
                ", aRank=" + aRank +
                '}';
    }
}
