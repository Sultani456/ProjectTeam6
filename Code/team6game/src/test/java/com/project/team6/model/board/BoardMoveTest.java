package com.project.team6.model.board;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.generators.BoardGenerator;
import com.project.team6.model.board.generators.barrierProperties.BarrierMode;
import com.project.team6.model.board.generators.barrierProperties.BarrierOptions;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests movement on a simple generated board.
 */
final class BoardMoveTest {

    private Board newEmptyBoard() {
        GameConfig.setBoardDimensions(5, 5);
        BoardGenerator gen = new BoardGenerator();
        BarrierOptions opts = new BarrierOptions(BarrierMode.NONE);
        BoardGenerator.Output out = gen.generate(opts);
        return new Board(out);
    }

    @Test
    void wallBlocksMove() {
        Board board = newEmptyBoard();

        MoveResult result = board.step(board.player(), Direction.LEFT);

        assertEquals(MoveResult.BLOCKED, result);
    }

    @Test
    void floorAllowsMove() {
        Board board = newEmptyBoard();
        Player player = board.player();

        MoveResult result = board.step(player, Direction.RIGHT);

        assertEquals(MoveResult.MOVED, result);
        assertEquals(new Position(1, player.position().row()), player.position());
    }
}
