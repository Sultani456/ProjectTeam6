package com.project.team6.model.characters.player;

// This stores info about a single move attempt.
public class MoveResult {
    public final MoveType type;   // what happened
    public final int fromX, fromY; // start position
    public final int toX, toY;     // end position
    public final char target;      // what was on the tile

    public MoveResult(MoveType type, int fromX, int fromY, int toX, int toY, char target) {
        this.type = type;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.target = target;
    }
}
