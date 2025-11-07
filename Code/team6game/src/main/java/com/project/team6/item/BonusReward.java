package com.project.team6.item;



/** Optional reward: not required to win (your 'o' tiles). */
public class BonusReward extends Reward {
    private final int amount;

    public BonusReward(int x, int y, int amount) {
        super(x, y);
        this.amount = amount;
    }

    @Override
    public char symbol() { return 'o'; }

    @Override
    public int value() { return amount; }

    @Override
    public boolean isRequiredToWin() { return false; }
}
