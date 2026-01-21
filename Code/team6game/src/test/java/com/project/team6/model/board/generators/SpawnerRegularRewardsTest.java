package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regular reward spawning should respect free cell capacity.
 */
final class SpawnerRegularRewardsTest {

    @Test
    void failsWhenNotEnoughFreeCells() {
        Board board = TestBoards.empty7x7();

        GameConfig.regularRewardCount = 1000;

        Spawner spawner = Spawner.withSeed(board, 40L);

        assertThrows(IllegalStateException.class, spawner::spawnRegularRewards);
    }
}
