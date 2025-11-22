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
 * Movement on the board.
 * Checks wall blocks and normal stepping.
 */
final class BoardMoveTest {

    @Test
    void wallBlocksMove() {
        // 5x5 with perimeter walls and empty inside
        BoardGenerator gen = new BoardGenerator();
        BoardGenerator.Output out = gen.generate(
                new BarrierOptions(5, 5, BarrierMode.NONE, null, null),
                0.0
        );
        Board b = new Board(out);

        // Move start is at west edge. A wall sits at x=0, but the cell to the west is out of bounds.
        MoveResult r = b.step(b.player(), Direction.LEFT);
        assertEquals(MoveResult.BLOCKED, r);
    }

    @Test
    void floorAllowsMove() {
        BoardGenerator gen = new BoardGenerator();
        BoardGenerator.Output out = gen.generate(
                new BarrierOptions(5, 5, BarrierMode.NONE, null, null),
                0.0
        );
        Board b = new Board(out);
        Player p = b.player();

        // Move right onto floor
        MoveResult r = b.step(p, Direction.RIGHT);
        assertEquals(MoveResult.MOVED, r);
        assertEquals(new Position(1, p.position().y()), p.position());
    }
}
