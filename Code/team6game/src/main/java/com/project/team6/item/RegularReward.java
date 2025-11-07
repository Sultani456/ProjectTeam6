package com.project.team6.item;

/** Regular reward: required for winning (your '.' tiles). */
// This class is the basic reward in the game.
// We need to collect these to finish the level.
// On the map it looks like a dot ('.').
public class RegularReward extends Reward {
    // This is how many points this reward gives.
    private final int amount;

    // Make a reward at grid (x, y) with a certain point amount.
    public RegularReward(int x, int y, int amount) {
        super(x, y);
        this.amount = amount;
    }

    // The map draws this reward as a '.' character.
    @Override
    public char symbol() { return '.'; }

    // This returns the points we get from this reward.
    @Override
    public int value() { return amount; }

    // This reward is needed to win the level.
    @Override
    public boolean isRequiredToWin() { return true; }
}
