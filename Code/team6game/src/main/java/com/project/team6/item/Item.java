package com.project.team6.item;



/** Base type for anything the player can step on and trigger. */
public abstract class Item {
    protected final int x;
    protected final int y;

    /** Display / identity symbol that matches your grid legend. */
    public abstract char symbol();

    /** Positive values add to score, negative subtract (punishment). */
    public abstract int value();

    /** Whether this item is required to win (i.e., regular reward). */
    public boolean isRequiredToWin() { return false; }

    public Item(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int getX() { return x; }
    public final int getY() { return y; }
}

