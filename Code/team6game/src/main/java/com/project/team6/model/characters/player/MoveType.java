package com.project.team6.model.characters.player;

// Different outcomes when the player tries to move.
public enum MoveType {
    MOVED,
    BLOCKED,
    COLLECTED_REQUIRED,
    COLLECTED_OPTIONAL,
    HIT_PUNISHMENT,
    REACHED_EXIT,
    HIT_ENEMY
}
