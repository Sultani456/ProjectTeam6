package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests bonus waves and timing. */
final class SpawnerBonusTest {

    @Test
    void spawnsBonusBatchAfterDelay() {
        Board board = TestBoards.empty7x7();
        Spawner spawner = new Spawner(board, 100);

        spawner.spawnBonusRewards(
                2,    // total to appear
                20,   // points per bonus
                1, 1, // spawn after about 1 second
                10,10 // long lifetime
        );

        for (int i = 0; i < 12; i++) {
            spawner.onTick();
        }

        assertTrue(board.hasActiveBonusRewards());
    }
}
