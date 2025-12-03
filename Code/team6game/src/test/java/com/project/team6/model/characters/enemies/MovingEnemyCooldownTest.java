package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * With period > 1 the enemy moves on the first tick,
 * then waits (period - 1) ticks before trying again.
 * The second attempt is blocked by START, so it stays put.
 */
final class MovingEnemyCooldownTest {

    @Test
    void movesOnFirstTickThenWaitsAndIsBlockedByStart() {
        Board b = TestBoards.empty7x7();             // player at START on west edge

        // Put enemy two cells to the right of the player on same row.
        Position s = b.start();
        Position e0 = new Position(Math.min(b.cols() - 1, s.column() + 2), s.row());
        MovingEnemy e = new MovingEnemy(e0, 3);      // period = 3
        b.registerEnemy(e);

        // Tick 1: moves one step toward player.
        b.tick(b.player().position());
        Position e1 = new Position(e0.column() - 1, e0.row());
        assertEquals(e1, e.position());

        // Tick 2 and 3: cooldown, stays.
        b.tick(b.player().position());
        assertEquals(e1, e.position());
        b.tick(b.player().position());
        assertEquals(e1, e.position());

        // Tick 4: tries to step onto START which is not enterable, so stays.
        b.tick(b.player().position());
        assertEquals(e1, e.position());
    }
}
