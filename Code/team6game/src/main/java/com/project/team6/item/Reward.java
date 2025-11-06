package com.project.team6.item;



/** Abstract reward; concrete types decide symbol/value/required-flag. */
public abstract class Reward extends Item {
    public Reward(int x, int y) {
        super(x, y);
    }
}
