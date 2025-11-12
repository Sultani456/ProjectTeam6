package com.project.team6.model.collectibles.rewards;

import com.project.team6.model.boardUtilities.Position;

/**
 * Optional reward with higher value. Supports optional lifetime (ticks).
 * If lifetime <= 0, it's persistent.
 */
public final class BonusReward extends Reward {

    private int lifetimeTicks; // 0 => disabled, <=0 means no expiry

    public BonusReward(Position position, int value) {
        super(position, value, false);
        this.lifetimeTicks = 0;
    }

    public BonusReward(Position position, int value, int lifetimeTicks) {
        super(position, value, false);
        this.lifetimeTicks = lifetimeTicks;
    }

    /** Returns true if still present; controller/board can remove when false. */
    public boolean onTickAndAlive() {
        if (lifetimeTicks > 0) {
            lifetimeTicks--;
            return lifetimeTicks > 0;
        }
        return true;
    }

    public int lifetimeRemaining() { return lifetimeTicks; }

    @Override public char symbol() { return 'o'; }
}
