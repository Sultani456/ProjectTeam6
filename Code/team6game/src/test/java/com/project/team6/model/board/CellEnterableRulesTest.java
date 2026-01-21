package com.project.team6.model.board;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enemies must not enter start or exit cells.
 */
final class CellEnterableRulesTest {

    @Test
    void enemyBlockedFromStart() {
        Board board = TestBoards.empty7x7();
        Position start = board.start();

        Position enemyPos = (start.column() + 1 < board.cols())
                ? new Position(start.column() + 1, start.row())
                : new Position(start.column() - 1, start.row());

        MovingEnemy enemy = new MovingEnemy(enemyPos, 1);
        board.registerEnemy(enemy);

        Direction towardStart = (enemyPos.column() > start.column()) ? Direction.LEFT : Direction.RIGHT;
        MoveResult result = board.step(enemy, towardStart);

        assertEquals(MoveResult.BLOCKED, result);
        assertEquals(enemyPos, enemy.position());
    }

    @Test
    void enemyBlockedFromExit() {
        Board board = TestBoards.empty7x7();
        Position exit = board.exit();

        Position enemyPos = (exit.column() - 1 >= 0)
                ? new Position(exit.column() - 1, exit.row())
                : new Position(exit.column() + 1, exit.row());

        MovingEnemy enemy = new MovingEnemy(enemyPos, 1);
        board.registerEnemy(enemy);

        Direction towardExit = (enemyPos.column() < exit.column()) ? Direction.RIGHT : Direction.LEFT;
        MoveResult result = board.step(enemy, towardExit);

        assertEquals(MoveResult.BLOCKED, result);
        assertEquals(enemyPos, enemy.position());
    }
}
