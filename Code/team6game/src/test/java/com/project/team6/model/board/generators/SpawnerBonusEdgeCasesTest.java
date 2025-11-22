package com.project.team6.model.board.generators;

import com.project.team6.controller.GameController;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bonus spawning: zero disables, equal ranges with nonzero delay
 * spawn once and then expire without instant respawn.
 */
final class SpawnerBonusEdgeCasesTest {

    @Test
    void zeroTotalDisablesBonuses() {
        Board b = TestBoards.empty7x7();
        Spawner sp = new Spawner(b, GameController.DEFAULT_TICK_MS);

        sp.spawnBonusRewards(0, 10, 1, 1, 1, 1);

        for (int i = 0; i < 10; i++) {
            b.tick(b.player().position());
            sp.onTick();
            assertFalse(b.hasActiveBonusRewards());
        }
    }

    @Test
    void fixedDelayAndLifetime_spawnOnce_thenExpire_noImmediateRespawn() {
        Board b = TestBoards.empty7x7();
        Spawner sp = new Spawner(b, GameController.DEFAULT_TICK_MS);

        // Nonzero delay to prevent instant respawn; lifetime is exactly 1 second.
        sp.spawnBonusRewards(
                2,      // quota
                5,      // points
                10, 10, // spawn delay = 10 sec
                1, 1    // lifetime = 1 sec
        );

        // Count down the delay
        int delayTicks = secondsToTicks(10, GameController.DEFAULT_TICK_MS);
        for (int i = 0; i < delayTicks; i++) {
            b.tick(b.player().position());
            sp.onTick();
        }

        // One more onTick triggers the spawn after countdown reaches zero
        sp.onTick();
        assertTrue(b.hasActiveBonusRewards());
        int before = countBonuses(b);
        assertTrue(before > 0);

        // Let them expire (lifetime 1s). After enough ticks, they should be gone.
        int lifeTicks = secondsToTicks(1, GameController.DEFAULT_TICK_MS);
        for (int i = 0; i < lifeTicks; i++) {
            b.tick(b.player().position());
            sp.onTick();
        }

        // Safety extra tick so Board purges any just-expired items
        b.tick(b.player().position());
        sp.onTick();

        assertEquals(0, countBonuses(b)); // expired and no immediate respawn (delay is 10s)
    }

    // ---- helpers ----
    private static int secondsToTicks(int seconds, int tickMillis) {
        if (seconds <= 0) return 0;
        double ticks = (seconds * 1000.0) / tickMillis;
        return Math.max(1, (int) Math.round(ticks));
    }

    private static int countBonuses(Board b) {
        int n = 0;
        for (int y = 0; y < b.rows(); y++) {
            for (int x = 0; x < b.cols(); x++) {
                if (b.cellAt(new Position(x, y)).item()
                        instanceof com.project.team6.model.collectibles.rewards.BonusReward) {
                    n++;
                }
            }
        }
        return n;
    }
}
