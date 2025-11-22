package com.project.team6.model.collectibles;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.collectibles.rewards.BonusReward;
import com.project.team6.model.collectibles.rewards.RegularReward;
import com.project.team6.model.runtime.Scoreboard;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests score changes when collecting items. */
final class CollectibleAndScoreTest {

    @Test
    void collectingAppliesToScoreboard() {
        Board b = TestBoards.empty7x7();

        var req = new RegularReward(new Position(2,3), 10);
        var opt = new BonusReward(new Position(3,3), 20, 0);
        var pun = new Punishment(new Position(4,3), -5);

        b.registerRegularReward(req);
        b.registerBonusReward(opt);
        b.registerPunishment(pun);

        Scoreboard s = new Scoreboard(0, 1);

        b.collectAt(req.position()).ifPresent(o -> s.collectedRequired(o.value()));
        b.collectAt(opt.position()).ifPresent(o -> s.collectedOptional(o.value()));
        b.collectAt(pun.position()).ifPresent(o -> s.penalize(o.value()));

        assertEquals(25, s.score());
        assertEquals(0, s.requiredRemaining());
    }
}
