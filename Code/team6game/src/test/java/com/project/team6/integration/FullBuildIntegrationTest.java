package com.project.team6.integration;

import com.project.team6.model.board.*;
import com.project.team6.model.board.generators.*;
import com.project.team6.model.board.generators.barrierProperties.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Builds a full board and spawns all item types.
 * Checks basic constraints and distances.
 */
final class FullBuildIntegrationTest {

    @Test
    void buildRandomBoardAndCheckConstraints() {
        int rows = 18, cols = 26;
        double barrierDensity = 0.15;

        BoardGenerator gen = new BoardGenerator();
        BarrierOptions opts = new BarrierOptions(rows, cols, BarrierMode.RANDOM, null, null);
        BoardGenerator.Output out = gen.generate(opts, barrierDensity);

        Board board = new Board(out);
        Spawner spawner = new Spawner(board, 120);

        spawner.spawnRegularRewards(8, 10);
        spawner.spawnBonusRewards(3, 20, 1, 2, 5, 6);
        spawner.spawnPunishments(5, -5);
        spawner.spawnEnemies(3, 6);

        // Start and Exit are not items
        assertNull(board.cellAt(board.start()).item());
        assertNull(board.cellAt(board.exit()).item());

        // Enemies are not too close to start or exit
        for (int y = 0; y < board.rows(); y++) {
            for (int x = 0; x < board.cols(); x++) {
                Cell c = board.cellAt(new Position(x, y));
                if (c.hasEnemy()) {
                    Position p = new Position(x, y);
                    assertTrue(Board.chebyshev(p, board.start()) >= 3);
                    assertTrue(Board.chebyshev(p, board.exit()) >= 3);
                }
            }
        }
    }
}
