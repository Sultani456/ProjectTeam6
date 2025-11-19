package com.project.team6.model.characters.enemies;

// This enemy always tries to move closer to the players

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

    /** Number of ticks between moves. Must be at least 1. */
    private final int movePeriod;  // >=1 ticks between moves

    /** Internal counter until the next move. */
    private int cooldown = 0;

    /**
     * Creates a moving enemy.
     *
     * @param position   starting position
     * @param movePeriod ticks between moves
     */
    public MovingEnemy(Position position, int movePeriod) {
        super(position);
        this.movePeriod = movePeriod;
    }

    /**
     * Runs once per tick.
     * Waits for cooldown, then moves and resets the cooldown.
     *
     * @param board     the game board
     * @param playerPos current player position
     */
    @Override
    public void tick(Board board, Position playerPos) {
        if (cooldown > 0) { cooldown--; return; }
        super.tick(board, playerPos); // will call board.step(...)
        cooldown = movePeriod - 1;
    }

    /**
     * Chooses a direction that reduces Manhattan distance to the player.
     * Prefers horizontal when horizontal distance is larger or equal.
     * Skips directions that are out of bounds or not walkable.
     *
     * @param board     the game board
     * @param playerPos current player position
     * @return direction to move or null to stay still
     */
    @Override
    public Direction decide(Board board, Position playerPos) {
        Position me = position();
        int dx = Integer.compare(playerPos.x(), me.x());
        int dy = Integer.compare(playerPos.y(), me.y());

        boolean horizFirst = Math.abs(playerPos.x() - me.x()) >= Math.abs(playerPos.y() - me.y());
        Direction direction1 = dx > 0 ? Direction.RIGHT : (dx < 0 ? Direction.LEFT : null);
        Direction direction2 = dy > 0 ? Direction.DOWN  : (dy < 0 ? Direction.UP   : null);
        Direction first  = horizFirst ? direction1 : direction2;
        Direction second = horizFirst ? direction2 : direction1;

        Direction[] order = order4(first, second);
        for (Direction d : order) {
            if (d == null) continue;
            Position to = new Position(me.x() + d.dx, me.y() + d.dy);
            if (board.isInBounds(to) && board.cellAt(to).isWalkableTerrain()) return d;
        }
        return null; // stuck
    }

    /**
     * Builds an ordered list of directions.
     * Places the two preferred directions first, then the rest.
     *
     * @param a first preferred direction
     * @param b second preferred direction
     * @return array with up to four unique directions
     */
    private static Direction[] order4(Direction a, Direction b) {
        Direction[] all = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
        java.util.LinkedHashSet<Direction> set = new java.util.LinkedHashSet<>();
        if (a != null) set.add(a);
        if (b != null) set.add(b);
        Collections.addAll(set, all);
        return set.toArray(new Direction[0]);
    }
}
