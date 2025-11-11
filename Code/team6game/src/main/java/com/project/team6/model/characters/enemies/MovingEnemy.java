package com.project.team6.model.characters.enemies;

// This enemy always tries to move closer to the players

import com.project.team6.model.boardUtilities.Board;
import com.project.team6.model.boardUtilities.Direction;
import com.project.team6.model.boardUtilities.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Greedy, Manhattan chaser: picks a 4-neighbor step that reduces distance,
 * preferring horizontal/vertical order that best approaches the player,
 * skipping illegal cells.
 */
public final class MovingEnemy extends Enemy {

    public MovingEnemy(int x, int y) { super(x, y); }

    @Override
    protected Direction decide(Board board, Position playerPos) {
        Position me = position();
        int dx = Integer.compare(playerPos.x(), me.x());
        int dy = Integer.compare(playerPos.y(), me.y());

        // Build preference list: move on the axis with larger |delta| first.
        List<Direction> prefs = new ArrayList<>(4);
        boolean horizFirst = Math.abs(playerPos.x() - me.x()) >= Math.abs(playerPos.y() - me.y());
        if (horizFirst) {
            if (dx > 0) prefs.add(Direction.RIGHT); else if (dx < 0) prefs.add(Direction.LEFT);
            if (dy > 0) prefs.add(Direction.DOWN);  else if (dy < 0) prefs.add(Direction.UP);
        } else {
            if (dy > 0) prefs.add(Direction.DOWN);  else if (dy < 0) prefs.add(Direction.UP);
            if (dx > 0) prefs.add(Direction.RIGHT); else if (dx < 0) prefs.add(Direction.LEFT);
        }
        // Add remaining directions as fallbacks
        if (!prefs.contains(Direction.UP))    prefs.add(Direction.UP);
        if (!prefs.contains(Direction.DOWN))  prefs.add(Direction.DOWN);
        if (!prefs.contains(Direction.LEFT))  prefs.add(Direction.LEFT);
        if (!prefs.contains(Direction.RIGHT)) prefs.add(Direction.RIGHT);

        // Pick the first legal enterable target
        for (Direction d : prefs) {
            Position to = new Position(me.x() + d.dx, me.y() + d.dy);
            if (board.tryEnter(to)) return d;
        }
        return null; // stuck
    }
}
