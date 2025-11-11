package com.project.team6.model.collectibles.rewards;

/**
 * Optional reward with higher value. Supports optional lifetime (ticks).
 * If lifetime <= 0, it's persistent.
 */
public final class BonusReward extends Reward {

    private int lifetimeTicks; // 0 => disabled, <=0 means no expiry

    public BonusReward(int x, int y, int value) {
        this(x, y, value, 0);
    }

    public BonusReward(int x, int y, int value, int lifetimeTicks) {
        super(x, y, value, false);
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
