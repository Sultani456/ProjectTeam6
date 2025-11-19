package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.CharacterObject;
import com.project.team6.model.board.*;

/**
 * Base enemy that moves at most one tile per tick.
 * Subclasses define the decision rule.
 */
public abstract class Enemy extends CharacterObject {

    /**
     * Creates an enemy at a starting position.
     *
     * @param position initial location on the board
     */
    protected Enemy(Position position) { super(position); }

    /**
     * Runs once per tick.
     * Chooses a direction, tries to step, then calls the post step hook.
     *
     * @param board     the game board
     * @param playerPos current player position
     */
    public void tick(Board board, Position playerPos) {
        Direction d = decide(board, playerPos);
        if (d == null) return;          // idle this tick
        MoveResult r = board.step(this, d); // MOVED / BLOCKED / COLLISION
        onPostStep(board, r);
    }

    /**
     * Hook that runs after a step attempt.
     * Subclasses can react to the move result.
     *
     * @param board  the game board
     * @param result move outcome from the board
     */
    protected void onPostStep(Board board, MoveResult result) { /* no-op */ }

    /**
     * Picks a direction to move this tick.
     * May return null to stay still.
     *
     * @param board     the game board
     * @param playerPos current player position
     * @return a direction or null
     */
    public abstract Direction decide(Board board, Position playerPos);

    /**
     * Returns the ASCII symbol used for enemies.
     *
     * @return 'B'
     */
    @Override public char symbol() { return 'B'; } // "B" for bad guy
}
