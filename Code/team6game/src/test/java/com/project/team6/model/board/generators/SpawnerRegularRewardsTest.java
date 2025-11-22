package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class SpawnerRegularRewardsTest {

    @Test
    void failsWhenNotEnoughFreeCells() {
        Board b = TestBoards.empty7x7();
        Spawner sp = new Spawner(b, 100);

        // empty7x7 has only a small number of free interior cells.
        // Asking for a huge number must hit the capacity check and throw.
        assertThrows(RuntimeException.class, () ->
                sp.spawnRegularRewards(1000, 10)
        );
    }
}
