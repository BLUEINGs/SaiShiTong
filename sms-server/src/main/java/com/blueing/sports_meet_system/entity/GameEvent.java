package com.blueing.sports_meet_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {
    private long timestamp;
    private String eventType; // "player_action", "ball_flying", "ball_in"
    private BallPosition ballPosition;
    private BasketPosition basketPosition;
    private PlayerAction playerAction;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEvent gameEvent = (GameEvent) o;
        // log.info("事件类型：{}=?{}",eventType,gameEvent.eventType);
        return Objects.equals(eventType, gameEvent.eventType) && Objects.equals(playerAction, gameEvent.playerAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, eventType, ballPosition, basketPosition, playerAction);
    }
}
