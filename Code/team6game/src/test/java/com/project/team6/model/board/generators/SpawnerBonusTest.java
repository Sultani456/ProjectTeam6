package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that bonus waves appear after some delay.
 */
final class SpawnerBonusTest {

    @Test
    void spawnsBonusBatchAfterDelay() {
        Board board = TestBoards.empty7x7();

        GameConfig.bonusRewardCount = 2;
        GameConfig.spawnMinTicks = 2;
        GameConfig.spawnMaxTicks = 2;
        GameConfig.lifeMinTicks = 5;
        GameConfig.lifeMaxTicks = 5;
        GameConfig.lifeRange = 1;

        Spawner spawner = Spawner.withSeed(board, 123L);
        spawner.spawnBonusRewards();

        for (int i = 0; i < 10 && !board.hasActiveBonusRewards(); i++) {
            board.tick(board.player().position());
            spawner.onTick();
        }

        assertTrue(board.hasActiveBonusRewards());
    }
}
