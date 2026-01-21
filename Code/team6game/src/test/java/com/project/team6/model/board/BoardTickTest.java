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
        Board board = TestBoards.empty7x7();

        board.step(board.player(), Direction.RIGHT);

        MovingEnemy enemy = new MovingEnemy(new Position(2, 3), 1);
        board.registerEnemy(enemy);

        TickSummary summary = board.tick(board.player().position());
        assertTrue(summary.playerCaught());
    }

    @Test
    void bonusExpiryRemovesFromCell() {
        Board board = TestBoards.empty7x7();

        BonusReward bonus = new BonusReward(new Position(2, 2), 1);
        board.registerCollectible(bonus);

        board.tick(board.player().position());

        assertNull(board.cellAt(new Position(2, 2)).item());
    }
}
