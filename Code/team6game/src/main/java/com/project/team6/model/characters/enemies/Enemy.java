package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.Position;

import java.util.Collections;

/**
 * Enemy that chases the player using Manhattan distance.
 * Tries to step closer every time it moves.
 * Prefers the axis that reduces distance the most.
 */
public final class MovingEnemy extends Enemy {

    /** Number of ticks between moves. Always >= 1. */
    private final int movePeriod;

    /** Internal counter until the next move. */
    private int cooldown = 0;

    /**
     * Creates a moving enemy.
     *
     * @param position   starting position
     * @param movePeriod ticks between moves, must be >= 1
     * @throws IllegalArgumentException if movePeriod < 1
     */
    public MovingEnemy(Position position, int movePeriod) {
        super(position);

        if (movePeriod < 1) {
            throw new IllegalArgumentException("movePeriod must be >= 1");
        }

        this.movePeriod = movePeriod;
    }

    @Override
    public void tick(Board board, Position playerPos) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        super.tick(board, playerPos);
        cooldown = movePeriod - 1;
    }

    @Override
    public Direction decide(Board board, Position playerPos) {
        Position me = position();
        int dColumn = Integer.compare(playerPos.column(), me.column());
        int dRow    = Integer.compare(playerPos.row(),    me.row());

        boolean horizFirst =
                Math.abs(playerPos.column() - me.column()) >=
                Math.abs(playerPos.row() - me.row());

        Direction first =
                horizFirst ? (dColumn > 0 ? Direction.RIGHT :
                             dColumn < 0 ? Direction.LEFT : null)
                           : (dRow > 0 ? Direction.DOWN :
                             dRow < 0 ? Direction.UP   : null);

        Direction second =
                horizFirst ? (dRow > 0 ? Direction.DOWN :
                             dRow < 0 ? Direction.UP   : null)
                           : (dColumn > 0 ? Direction.RIGHT :
                             dColumn < 0 ? Direction.LEFT : null);

        Direction[] order = order4(first, second);

        for (Direction d : order) {
            if (d == null) continue;
            Position to = new Position(me.column() + d.d_column, me.row() + d.d_row);
            if (board.isInBounds(to) && board.cellAt(to).isWalkableTerrain()) {
                return d;
            }
        }
        return null;
    }

    private static Direction[] order4(Direction a, Direction b) {
        Direction[] all = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
        java.util.LinkedHashSet<Direction> set = new java.util.LinkedHashSet<>();
        if (a != null) set.add(a);
        if (b != null) set.add(b);
        Collections.addAll(set, all);
        return set.toArray(new Direction[0]);
    }
}
