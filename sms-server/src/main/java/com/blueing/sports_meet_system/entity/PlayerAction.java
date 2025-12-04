package com.blueing.sports_meet_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAction {
    private String actionType; // "passing" or "shooting"
    private int[] playerColor; // RGB颜色
    private int teId; // 队伍Id
    private float confidence;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAction that = (PlayerAction) o;
        // log.info("teId：{}=?{}；动作类型：{}=?{}",teId,that.teId,actionType,that.actionType);
        return teId == that.teId && Objects.equals(actionType, that.actionType) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionType, Arrays.hashCode(playerColor), teId, confidence);
    }

    public PlayerAction(String s, int[] holderColor, float v) {
        this.actionType=s;
        this.playerColor=holderColor;
        this.confidence=v;
    }
}
