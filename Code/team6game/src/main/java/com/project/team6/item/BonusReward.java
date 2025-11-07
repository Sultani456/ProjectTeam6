package com.project.team6.item;

// This class is for bonus rewards.
// It gives extra points, but it is not needed to finish the game.
/** Optional reward: not required to win (your 'o' tiles). */
public class BonusReward extends Reward {
    // This is how many points this bonus gives.
    private final int amount;

    // We set the position (x, y) and how many points it gives.
    public BonusReward(int x, int y, int amount) {
        super(x, y);
        this.amount = amount;
    }

    // On the map, this bonus shows as the letter 'o'.
    @Override
    public char symbol() { return 'o'; }

    // This returns the points you get from this bonus.
    @Override
    public int value() { return amount; }

    // This bonus is not needed to win the level.
    @Override
    public boolean isRequiredToWin() { return false; }
}
