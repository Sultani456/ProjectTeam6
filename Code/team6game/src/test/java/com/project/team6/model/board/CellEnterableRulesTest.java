package com.project.team6.model.board;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enemies must not enter START or EXIT cells.
 * Uses board.start()/board.exit() so it does not assume fixed coordinates.
 */
final class CellEnterableRulesTest {

    @Test
    void enemyBlockedFromStart() {
        Board b = TestBoards.empty7x7();
        Position s = b.start();

        // Place enemy right of start if possible, otherwise left.
        Position enemyPos = (s.column() + 1 < b.cols())
                ? new Position(s.column() + 1, s.row())
                : new Position(s.column() - 1, s.row());

        MovingEnemy e = new MovingEnemy(enemyPos, 1);
        b.registerEnemy(e);

        Direction towardStart = (enemyPos.column() > s.column()) ? Direction.LEFT : Direction.RIGHT;
        MoveResult r = b.step(e, towardStart);
        assertEquals(MoveResult.BLOCKED, r);
        assertEquals(enemyPos, e.position());
    }

    @Test
    void enemyBlockedFromExit() {
        Board b = TestBoards.empty7x7();
        Position ex = b.exit();

        // Place enemy left of exit if possible, otherwise right.
        Position enemyPos = (ex.column() - 1 >= 0)
                ? new Position(ex.column() - 1, ex.row())
                : new Position(ex.column() + 1, ex.row());

        MovingEnemy e = new MovingEnemy(enemyPos, 1);
        b.registerEnemy(e);

        Direction towardExit = (enemyPos.column() < ex.column()) ? Direction.RIGHT : Direction.LEFT;
        MoveResult r = b.step(e, towardExit);
        assertEquals(MoveResult.BLOCKED, r);
        assertEquals(enemyPos, e.position());
    }
}
