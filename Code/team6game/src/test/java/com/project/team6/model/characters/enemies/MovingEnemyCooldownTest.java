package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cooldown movement for moving enemies.
 */
final class MovingEnemyCooldownTest {

    @Test
    void movesOnFirstTickThenWaitsAndIsBlockedByStart() {
        Board board = TestBoards.empty7x7();

        Position start = board.start();
        Position initialEnemyPos = new Position(Math.min(board.cols() - 1, start.column() + 2), start.row());
        MovingEnemy enemy = new MovingEnemy(initialEnemyPos, 3);
        board.registerEnemy(enemy);

        board.tick(board.player().position());
        Position afterFirst = new Position(initialEnemyPos.column() - 1, initialEnemyPos.row());
        assertEquals(afterFirst, enemy.position());

        board.tick(board.player().position());
        assertEquals(afterFirst, enemy.position());
        board.tick(board.player().position());
        assertEquals(afterFirst, enemy.position());

        board.tick(board.player().position());
        assertEquals(afterFirst, enemy.position());
    }
}
