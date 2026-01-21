package com.project.team6.model.board;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.Player;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests movement and collision results on the test board.
 */
final class BoardStepTest {

    @Test
    void moveToFloor_succeeds() {
        Board board = TestBoards.empty7x7();
        Player player = board.player();

        assertEquals(MoveResult.MOVED, board.step(player, Direction.RIGHT));
        assertEquals(new Position(1, 3), player.position());
    }

    @Test
    void moveIntoWall_blocked() {
        Board board = TestBoards.empty7x7();
        Player player = board.player();

        assertEquals(MoveResult.BLOCKED, board.step(player, Direction.UP));
        assertEquals(new Position(0, 3), player.position());
    }

    @Test
    void moveIntoEnemy_isCollision() {
        Board board = TestBoards.empty7x7();
        MovingEnemy enemy = new MovingEnemy(new Position(1, 3), 1);
        board.registerEnemy(enemy);

        Player player = board.player();
        assertEquals(MoveResult.COLLISION, board.step(player, Direction.RIGHT));
        assertEquals(new Position(1, 3), player.position());
    }
}
