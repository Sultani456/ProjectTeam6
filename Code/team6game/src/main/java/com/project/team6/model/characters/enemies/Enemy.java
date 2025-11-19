package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.CharacterObject;

import com.project.team6.model.board.*;

/**
 * Base enemy with a one-tile-per-tick movement contract.
 */
public abstract class Enemy extends CharacterObject {

    protected Enemy(Position position) { super(position); }

    /**
     * Called once per tick by the controller.
     * Default: move one step following the decision rule returned by decide().
     */
    public void tick(Board board, Position playerPos) {
        Direction d = decide(board, playerPos);
        if (d == null) return;          // idle this tick
        MoveResult r = board.step(this, d); // MOVED / BLOCKED / COLLISION
        onPostStep(board, r);
    }

    /** Optional hook for subclasses after stepping. */
    protected void onPostStep(Board board, MoveResult result) { /* no-op */ }

    /** Choose a direction (may return null to stay still). */
    public abstract Direction decide(Board board, Position playerPos);

    @Override public char symbol() { return 'B'; } // “B” for bad guy
}
