package com.blueing.sports_meet_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {
    private long timestamp;
    private String eventType; // "player_action", "ball_flying", "ball_in"
    private BallPosition ballPosition;
    private BasketPosition basketPosition;
    private PlayerAction playerAction;
}
