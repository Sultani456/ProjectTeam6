package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.CharacterObject;
import com.project.team6.model.board.Board;

/**
 * Base enemy that moves at most one tile per tick.
 * Subclasses define the decision rule.
 */
public abstract class Enemy extends CharacterObject {

    /**
     * Creates an enemy at a starting position.
     *
     * @param position initial location
     */
    protected Enemy(Position position) {
        super(position);
    }

    /**
     * Runs once per tick.
     * Chooses a direction, attempts to step, then calls the hook.
     *
     * @param board     current board
     * @param playerPos player position
     */
    public abstract void tick(Board board, Position playerPos);

    /**
     * Chooses a movement direction.
     * May return null to stay still.
     *
     * @param board     current board
     * @param playerPos player position
     * @return direction to move or null
     */
    public abstract Direction decide(Board board, Position playerPos);

    /**
     * Enemies can walk on normal terrain but not START/EXIT or occupied tiles.
     */
    @Override
    public boolean canEnter(Cell cell) {
        if (!cell.isWalkableTerrain()) return false;
        if (cell.terrain() == Cell.Terrain.START || cell.terrain() == Cell.Terrain.EXIT) return false;
        return cell.enemy() == null;
    }

    /** ASCII symbol for enemies. */
    @Override
    public char symbol() {
        return 'B';
    }
}
