package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.collectibles.Punishment;
import com.project.team6.model.collectibles.rewards.RegularReward;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Punishments should not break paths between start, exit, and rewards.
 */
final class SpawnerPunishmentPathTest {

    @Test
    void startExitAndRewardsRemainReachableWhenTreatingPunishmentsAsBlocked() {
        Board board = TestBoards.empty7x7();

        board.registerCollectible(new RegularReward(new Position(2, 2)));
        board.registerCollectible(new RegularReward(new Position(4, 4)));

        GameConfig.numPunishments = 3;

        Spawner spawner = Spawner.withSeed(board, 30L);
        spawner.spawnPunishments();

        Set<Position> blocked = new HashSet<>();
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position p = new Position(col, row);
                Cell c = board.cellAt(p);
                if (c.item() instanceof Punishment) {
                    blocked.add(p);
                }
            }
        }

        Position start = board.start();
        Position exit  = board.exit();

        assertTrue(SpawnerHelper.canReach(board, start, exit, blocked));

        for (var rr : board.regularRewards()) {
            assertTrue(SpawnerHelper.canReach(board, start, rr.position(), blocked));
        }
    }
}
