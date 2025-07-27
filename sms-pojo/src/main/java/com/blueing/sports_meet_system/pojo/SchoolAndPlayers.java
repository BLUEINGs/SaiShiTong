package com.blueing.sports_meet_system.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SchoolAndPlayers extends School {

    private List<Player> players;

    public SchoolAndPlayers(Integer scId, String name, String slogan, String img, Integer playerCount, Integer appCount, List<Player> players) {
        super(scId,null,name,null, slogan, img, playerCount, appCount);
        this.players = players;
    }

    public SchoolAndPlayers(School school,List<Player> players) {
        super(school.getScId(), null,null,school.getName(), school.getSlogan(), school.getImg(), school.getPlayerCount(), school.getAppCount());
        this.players = players;
    }

}
