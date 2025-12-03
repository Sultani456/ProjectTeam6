package com.project.team6.model.collectibles.rewards;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Position;

/**
 * Optional reward with a higher value than regular rewards.
 * It can be temporary based on its lifetime in ticks.
 */
public final class BonusReward extends Reward {

    /** Remaining lifetime in ticks. A value of 0 or less means it does not expire. */
    private int lifetimeTicks;

    /**
     * Creates a bonus reward.
     *
     * @param position       board position of the bonus
     * @param lifetimeTicks  ticks to live. 0 or less means persistent
     */
    public BonusReward(Position position, int lifetimeTicks) {
        super(position, GameConfig.bonusPoints, /*requiredToWin=*/false);
        this.lifetimeTicks = lifetimeTicks;
    }

    /**
     * Updates the lifetime and reports if the bonus should remain.
     * Call this once per game tick while the bonus is on the board.
     *
     * @return true if still alive, false if it has expired
     */
    public boolean onTickAndAlive() {
        if (lifetimeTicks > 0) {
            lifetimeTicks--;
            return lifetimeTicks > 0;
        }
        // lifetime <= 0 means no expiry
        return true;
    }

    /**
     * Returns the ASCII symbol used for this bonus.
     *
     * @return 'o'
     */
    @Override
    public char symbol() {
        return 'o';
    }
}
