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
        Position enemyPos = (s.x() + 1 < b.cols())
                ? new Position(s.x() + 1, s.y())
                : new Position(s.x() - 1, s.y());

        MovingEnemy e = new MovingEnemy(enemyPos, 1);
        b.registerEnemy(e);

        Direction towardStart = (enemyPos.x() > s.x()) ? Direction.LEFT : Direction.RIGHT;
        MoveResult r = b.step(e, towardStart);
        assertEquals(MoveResult.BLOCKED, r);
        assertEquals(enemyPos, e.position());
    }

    @Test
    void enemyBlockedFromExit() {
        Board b = TestBoards.empty7x7();
        Position ex = b.exit();

        // Place enemy left of exit if possible, otherwise right.
        Position enemyPos = (ex.x() - 1 >= 0)
                ? new Position(ex.x() - 1, ex.y())
                : new Position(ex.x() + 1, ex.y());

        MovingEnemy e = new MovingEnemy(enemyPos, 1);
        b.registerEnemy(e);

        Direction towardExit = (enemyPos.x() < ex.x()) ? Direction.RIGHT : Direction.LEFT;
        MoveResult r = b.step(e, towardExit);
        assertEquals(MoveResult.BLOCKED, r);
        assertEquals(enemyPos, e.position());
    }
}
