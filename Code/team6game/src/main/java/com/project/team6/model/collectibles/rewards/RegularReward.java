package com.project.team6.model.collectibles.rewards;

/** Mandatory reward; needed to win. */
public final class RegularReward extends Reward {

    public RegularReward(int x, int y, int value) {
        super(x, y, value, true);
    }

    @Override public char symbol() { return '.'; }
}

