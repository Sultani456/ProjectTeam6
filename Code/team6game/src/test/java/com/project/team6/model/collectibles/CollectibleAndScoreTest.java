package com.project.team6.model.collectibles;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.collectibles.rewards.BonusReward;
import com.project.team6.model.collectibles.rewards.RegularReward;
import com.project.team6.model.runtime.Scoreboard;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests how different collectibles change the scoreboard.
 */
final class CollectibleAndScoreTest {

    @Test
    void collectingItemsUpdatesScoreAndRequiredCount() {
        Board board = TestBoards.empty7x7();

        RegularReward required = new RegularReward(new Position(2, 3));
        BonusReward optional = new BonusReward(new Position(3, 3), 0);
        Punishment punishment = new Punishment(new Position(4, 3));

        board.registerCollectible(required);
        board.registerCollectible(optional);
        board.registerCollectible(punishment);

        GameConfig.regularRewardCount = 1;
        Scoreboard scoreboard = new Scoreboard();

        board.collectAt(required.position()).ifPresent(o -> scoreboard.collectedRequired(o.value()));
        board.collectAt(optional.position()).ifPresent(o -> scoreboard.collectedOptional(o.value()));
        board.collectAt(punishment.position()).ifPresent(o -> scoreboard.penalize(o.value()));

        int expectedScore = GameConfig.regularPoints + GameConfig.bonusPoints + GameConfig.punishmentPenalty;

        assertEquals(expectedScore, scoreboard.score());
        assertEquals(0, scoreboard.requiredRemaining());
    }
}
