package com.project.team6.model.board;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.Player;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests movement and collision results. */
final class BoardStepTest {

    @Test
    void moveToFloor_succeeds() {
        Board b = TestBoards.empty7x7();
        Player p = b.player();

        assertEquals(MoveResult.MOVED, b.step(p, Direction.RIGHT));
        assertEquals(new Position(1,3), p.position());
    }

    @Test
    void moveIntoWall_blocked() {
        Board b = TestBoards.empty7x7();
        Player p = b.player();
        assertEquals(MoveResult.BLOCKED, b.step(p, Direction.UP));
        assertEquals(new Position(0,3), p.position());
    }

    @Test
    void moveIntoEnemy_isCollision() {
        Board b = TestBoards.empty7x7();
        MovingEnemy e = new MovingEnemy(new Position(1,3), 1);
        b.registerEnemy(e);

        Player p = b.player();
        assertEquals(MoveResult.COLLISION, b.step(p, Direction.RIGHT));
        assertEquals(new Position(1,3), p.position());
    }
}
