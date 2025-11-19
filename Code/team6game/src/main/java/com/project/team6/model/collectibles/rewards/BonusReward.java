package com.project.team6.model.collectibles.rewards;

import com.project.team6.model.board.Position;

/**
 * Optional reward with higher value.
 * Can be temporary: if lifetimeTicks > 0, it disappears after that many ticks.
 * If lifetimeTicks <= 0, it is persistent.
 */
public final class BonusReward extends Reward {

    /** Remaining lifetime in ticks. <= 0 means "no expiry / persistent". */
    private int lifetimeTicks;

    public BonusReward(Position position, int value, int lifetimeTicks) {
        super(position, value, /*requiredToWin=*/false);
        this.lifetimeTicks = lifetimeTicks;
    }

    /**
     * Called once per game tick while this bonus is on the board.
     *
     * @return true if the bonus is still alive and should remain on the board;
     *         false if it has expired and can be removed by Board.tick(...).
     */
    public boolean onTickAndAlive() {
        if (lifetimeTicks > 0) {
            lifetimeTicks--;
            return lifetimeTicks > 0;
        }
        // lifetime <= 0 ⇒ no expiry
        return true;
    }

    @Override
    public char symbol() {
        // ASCII representation used in SYMBOL render mode
        return 'o';
    }
}

