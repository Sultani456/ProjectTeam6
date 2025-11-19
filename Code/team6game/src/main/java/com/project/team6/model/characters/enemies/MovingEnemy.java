package com.project.team6.model.characters.enemies;

// This enemy always tries to move closer to the players

import com.project.team6.model.board.Board;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.Position;

import java.util.Collections;

/**
 * Greedy, Manhattan chaser: picks a 4-neighbor step that reduces distance,
 * preferring horizontal/vertical order that best approaches the player,
 * skipping illegal cells.
 */
public final class MovingEnemy extends Enemy {
    private final int movePeriod;  // >=1 ticks between moves
    private int cooldown = 0;

    public MovingEnemy(Position position, int movePeriod) {
        super(position);
        this.movePeriod = movePeriod;
    }

    @Override
    public void tick(Board board, Position playerPos) {
        if (cooldown > 0) { cooldown--; return; }
        super.tick(board, playerPos); // will call board.step(...)
        cooldown = movePeriod - 1;
    }

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

    private static Direction[] order4(Direction a, Direction b) {
        Direction[] all = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
        java.util.LinkedHashSet<Direction> set = new java.util.LinkedHashSet<>();
        if (a != null) set.add(a);
        if (b != null) set.add(b);
        Collections.addAll(set, all);
        return set.toArray(new Direction[0]);
    }
}
