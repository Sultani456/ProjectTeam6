package com.project.team6.model.characters.enemies;

import com.project.team6.model.characters.CharacterObject;

import com.project.team6.model.boardUtilities.Board;
import com.project.team6.model.boardUtilities.Direction;
import com.project.team6.model.boardUtilities.Position;

/**
 * Base enemy with a one-tile-per-tick movement contract.
 */
public abstract class Enemy extends CharacterObject {

    protected Enemy(int x, int y) { super(x, y); }

    /**
     * Called once per tick by the controller.
     * Default: move one step following the decision rule returned by decide().
     */
    public void tick(Board board, Position playerPos) {
        Direction d = decide(board, playerPos);
        if (d != null) tryMove(board, d);
    }

    /** Choose a direction (may return null to stay still). */
    protected abstract Direction decide(Board board, Position playerPos);

    @Override public char symbol() { return 'B'; } // “B” for bad guy
}
