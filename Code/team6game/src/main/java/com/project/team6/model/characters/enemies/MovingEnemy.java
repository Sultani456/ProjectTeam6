package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;

import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Enemy that chases the player using Manhattan distance.
 * Moves once every movePeriod ticks.
 */
public final class MovingEnemy extends Enemy {

    /** Ticks between moves. Must be >= 1. */
    private final int movePeriod;

    /** Internal cooldown before next move. */
    private int cooldown = 0;

    /**
     * Creates a moving enemy.
     *
     * @param position   starting position
     * @param movePeriod ticks between moves (must be >= 1)
     */
    public MovingEnemy(Position position, int movePeriod) {
        super(position);
        if (movePeriod < 1) {
            throw new IllegalArgumentException("movePeriod must be >= 1");
        }
        this.movePeriod = movePeriod;
    }

    /**
     * Runs once per tick.
     * Waits out cooldown, then moves.
     */
    @Override
    public void tick(Board board, Position playerPos) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Direction d = decide(board, playerPos);
        if (d == null) {
            return; // Stays still this tick
        }

        MoveResult result = board.step(this, d);


        cooldown = movePeriod - 1;
    }

    /**
     * Chooses the direction that reduces Manhattan distance to the player.
     */
    @Override
    public Direction decide(Board board, Position playerPos) {
        Position currentPos = position(); // renamed from "me"

        int dx = Integer.compare(playerPos.column(), currentPos.column());
        int dy = Integer.compare(playerPos.row(), currentPos.row());

        boolean preferHorizontal =
            Math.abs(playerPos.column() - currentPos.column()) >=
            Math.abs(playerPos.row() - currentPos.row());

        Direction horiz = dx > 0 ? Direction.RIGHT : (dx < 0 ? Direction.LEFT : null);
        Direction vert  = dy > 0 ? Direction.DOWN  : (dy < 0 ? Direction.UP   : null);

        Direction first  = preferHorizontal ? horiz : vert;
        Direction second = preferHorizontal ? vert  : horiz;

        for (Direction d : preferredOrder(first, second)) {
            if (d == null) continue;

            Position to = new Position(
                    currentPos.column() + d.d_column,
                    currentPos.row() + d.d_row
            );

            if (board.isInBounds(to) && board.cellAt(to).isWalkableTerrain()) {
                return d;
            }
        }

        return null; // No valid direction
    }

    /**
     * Returns the preferred movement order: first, second, then all others.
     */
    private static Direction[] preferredOrder(Direction a, Direction b) {
        LinkedHashSet<Direction> set = new LinkedHashSet<>();
        if (a != null) set.add(a);
        if (b != null) set.add(b);
        Collections.addAll(set, Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
        return set.toArray(new Direction[0]);
    }
}
