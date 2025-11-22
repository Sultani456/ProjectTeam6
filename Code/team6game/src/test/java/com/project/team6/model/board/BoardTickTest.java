package com.project.team6.model.board;

import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.TickSummary;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.model.collectibles.rewards.BonusReward;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests enemy ticks and bonus expiry.
 */
final class BoardTickTest {

    @Test
    void enemyMovesAndCanCatchPlayer() {
        Board b = TestBoards.empty7x7();

        // Move player off START to a floor cell (1,3).
        b.step(b.player(), Direction.RIGHT);

        // Put enemy at (2,3). On tick it moves left into the player.
        MovingEnemy e = new MovingEnemy(new Position(2,3), 1);
        b.registerEnemy(e);

        TickSummary t = b.tick(b.player().position());
        assertTrue(t.playerCaught());
    }

    @Test
    void bonusExpiryRemovesFromCell() {
        Board b = TestBoards.empty7x7();

        // Lifetime 1 means it expires on the first tick.
        BonusReward bonus = new BonusReward(new Position(2,2), 20, 1);
        b.registerBonusReward(bonus);

        b.tick(b.player().position()); // expires and is removed
        assertNull(b.cellAt(new Position(2,2)).item());
    }
}
