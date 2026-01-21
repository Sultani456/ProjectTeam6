package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests moving enemy logic and cooldown behavior.
 */
final class MovingEnemyTest {

    @Test
    void respectsMovePeriodCooldown() {
        Board board = TestBoards.empty7x7();
        Position start = new Position(2, 3);
        MovingEnemy enemy = new MovingEnemy(start, 3);
        board.registerEnemy(enemy);

        board.tick(board.player().position());
        Position afterFirst = enemy.position();

        board.tick(board.player().position());
        board.tick(board.player().position());
        Position afterThird = enemy.position();

        assertNotEquals(start, afterFirst);
        assertEquals(afterFirst, afterThird);
    }

    @Test
    void choosesDirectionThatReducesManhattan() {
        Board board = TestBoards.empty7x7();
        MovingEnemy enemy = new MovingEnemy(new Position(5, 1), 1);
        board.registerEnemy(enemy);

        Direction d = enemy.decide(board, board.player().position());
        assertTrue(d == Direction.LEFT || d == Direction.DOWN);
    }
}
