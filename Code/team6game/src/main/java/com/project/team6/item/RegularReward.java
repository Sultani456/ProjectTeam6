package com.project.team6.item;



/** Regular reward: required for winning (your '.' tiles). */
public class RegularReward extends Reward {
    private final int amount;

    public RegularReward(int x, int y, int amount) {
        super(x, y);
        this.amount = amount;
    }

    @Override
    public char symbol() { return '.'; }

    @Override
    public int value() { return amount; }

    @Override
    public boolean isRequiredToWin() { return true; }
}
