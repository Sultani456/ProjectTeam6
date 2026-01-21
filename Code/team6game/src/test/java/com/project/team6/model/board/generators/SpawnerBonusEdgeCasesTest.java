package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra tests for bonus wave setup.
 */
final class SpawnerBonusEdgeCasesTest {

    /**
     * When total bonus count is zero, no bonus should ever appear.
     */
    @Test
    void zeroTotalDisablesBonuses() {
        Board board = TestBoards.empty7x7();
        GameConfig.bonusRewardCount = 0;

        Spawner spawner = Spawner.withSeed(board, 1L);
        spawner.spawnBonusRewards();

        for (int i = 0; i < 10; i++) {
            board.tick(board.player().position());
            spawner.onTick();
            assertFalse(board.hasActiveBonusRewards());
        }
    }

    /**
     * Asking for too many bonus rewards throws an error.
     */
    @Test
    void tooManyBonusesThrowException() {
        Board board = TestBoards.empty7x7();

        GameConfig.bonusRewardCount = 1000;

        Spawner spawner = Spawner.withSeed(board, 2L);

        assertThrows(IllegalArgumentException.class, spawner::spawnBonusRewards);
    }
}
