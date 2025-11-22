package com.project.team6.model.board;

import com.project.team6.model.board.generators.BoardGenerator;
import com.project.team6.model.board.generators.barrierProperties.BarrierMode;
import com.project.team6.model.board.generators.barrierProperties.BarrierOptions;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests how movement works on the board.
 * These tests check when a player is blocked by walls and when the player can move normally.
 */
final class BoardMoveTest {

    @Test
    void wallBlocksMove() {
        // Creates a 5x5 board with walls around the edges and empty cells inside.
        BoardGenerator gen = new BoardGenerator();
        BoardGenerator.Output out = gen.generate(
                new BarrierOptions(5, 5, BarrierMode.NONE, null, null),
                0.0
        );
        Board b = new Board(out);

        // The player starts near the west edge. Moving left should fail because the space is out of bounds.
        MoveResult r = b.step(b.player(), Direction.LEFT);
        assertEquals(MoveResult.BLOCKED, r);
    }

    @Test
    void floorAllowsMove() {
        // Creates a simple 5x5 board with no barriers.
        BoardGenerator gen = new BoardGenerator();
        BoardGenerator.Output out = gen.generate(
                new BarrierOptions(5, 5, BarrierMode.NONE, null, null),
                0.0
        );
        Board b = new Board(out);
        Player p = b.player();

        // The player moves right onto an empty floor cell.
        MoveResult r = b.step(p, Direction.RIGHT);
        assertEquals(MoveResult.MOVED, r);
        assertEquals(new Position(1, p.position().y()), p.position());
    }
}
