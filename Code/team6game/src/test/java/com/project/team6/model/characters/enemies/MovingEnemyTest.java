package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests moving enemy logic and cooldown. */
final class MovingEnemyTest {

    @Test
    void respectsMovePeriodCooldown() {
        Board b = TestBoards.empty7x7();
        Position start = new Position(2,3);
        MovingEnemy e = new MovingEnemy(start, 3);
        b.registerEnemy(e);

        b.tick(b.player().position()); // should move
        Position after1 = e.position();
        b.tick(b.player().position()); // wait
        b.tick(b.player().position()); // wait
        Position after3 = e.position();

        assertNotEquals(start, after1);
        assertEquals(after1, after3);
    }

    @Test
    void choosesDirectionThatReducesManhattan() {
        Board b = TestBoards.empty7x7();
        MovingEnemy e = new MovingEnemy(new Position(5,1), 1);
        b.registerEnemy(e);

        Direction d = e.decide(b, b.player().position());
        assertTrue(d == Direction.LEFT || d == Direction.DOWN);
    }
}
