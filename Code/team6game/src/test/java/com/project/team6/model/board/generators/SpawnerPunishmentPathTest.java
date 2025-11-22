package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.collectibles.rewards.RegularReward;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Tests that punishments keep paths safe. */
final class SpawnerPunishmentPathTest {

    @Test
    void startToExitAndRewardsRemainReachable() {
        Board b = TestBoards.empty7x7();
        b.registerRegularReward(new RegularReward(new Position(2,2), 10));
        b.registerRegularReward(new RegularReward(new Position(4,4), 10));

        Spawner spawner = new Spawner(b, 100);
        spawner.spawnPunishments(3, -5);

        var start = b.start();
        var exit  = b.exit();

        assertTrue(SpawnerHelper.canReach(b, start, exit, Set.of()));
        for (var rr : b.regularRewards()) {
            assertTrue(SpawnerHelper.canReach(b, start, rr.position(), Set.of()));
        }
    }
}
