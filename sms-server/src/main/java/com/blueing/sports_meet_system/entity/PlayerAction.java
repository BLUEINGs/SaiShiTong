package com.blueing.sports_meet_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAction {
    private String actionType; // "passing" or "shooting"
    private int[] playerColor; // RGB颜色
    private float confidence;
}
