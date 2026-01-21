package com.project.team6.integration;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.BoardGenerator;
import com.project.team6.model.board.generators.Spawner;
import com.project.team6.model.board.generators.barrierProperties.BarrierMode;
import com.project.team6.model.board.generators.barrierProperties.BarrierOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Builds a full random board and spawns all item types and enemies.
 */
final class FullBuildIntegrationTest {

    @Test
    void buildRandomBoardAndCheckConstraints() {
        int rows = 18;
        int cols = 26;
        double barrierDensity = 0.15;

        GameConfig.setBoardDimensions(rows, cols);
        GameConfig.boardBarrierPercentage = barrierDensity;

        GameConfig.regularRewardCount = 8;
        GameConfig.regularPoints = 10;

        GameConfig.bonusRewardCount = 3;
        GameConfig.bonusPoints = 20;

        GameConfig.numPunishments = 5;
        GameConfig.punishmentPenalty = -5;

        GameConfig.numEnemies = 3;
        GameConfig.enemyMovePeriod = 6;

        BoardGenerator gen = new BoardGenerator();
        BarrierOptions opts = new BarrierOptions(BarrierMode.RANDOM);
        BoardGenerator.Output out = gen.generate(opts);

        Board board = new Board(out);
        Spawner spawner = Spawner.withSeed(board, 1234L);

        spawner.spawnRegularRewards();
        spawner.spawnPunishments();
        spawner.spawnEnemies();
        spawner.spawnBonusRewards();

        // Start and Exit should not hold items
        assertNull(board.cellAt(board.start()).item());
        assertNull(board.cellAt(board.exit()).item());

        // Enemies should be at least 3 tiles away from start and exit
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Cell c = board.cellAt(new Position(col, row));
                if (c.hasEnemy()) {
                    Position p = new Position(col, row);
                    assertTrue(Board.chebyshev(p, board.start()) >= 3);
                    assertTrue(Board.chebyshev(p, board.exit()) >= 3);
                }
            }
        }
    }
}
