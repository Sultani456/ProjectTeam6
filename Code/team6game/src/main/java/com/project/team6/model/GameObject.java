package com.project.team6.model;

// This class is the parent for all items a player can step on.
// Other item types will extend this class.

/** Base type for anything the player can step on and trigger. */
public abstract class GameObject {
    // Grid x position for this item.
    protected final int x;
    // Grid y position for this item.
    protected final int y;

    // The character we show on the map for this item.
    /** Display / identity symbol that matches your grid legend. */
    public abstract char symbol();

    // Points this item gives or takes. Positive adds, negative subtracts.
    /** Positive values add to score, negative subtract (punishment). */
    public abstract int value();

    // If true, you must collect this to win the level. Default is false.
    /** Whether this item is required to win (i.e., regular reward). */
    public boolean isRequiredToWin() { return false; }

    // Save the position when we make the item.
    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Give back the x position.
    public final int getX() { return x; }
    // Give back the y position.
    public final int getY() { return y; }
}
