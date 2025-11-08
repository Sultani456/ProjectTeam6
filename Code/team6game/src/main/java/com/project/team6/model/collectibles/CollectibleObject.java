package com.project.team6.model.collectibles;

import com.project.team6.model.GameObject;

public abstract class CollectibleObject extends GameObject {
    public CollectibleObject(int x, int y) {
        super(x, y);
    }
    // The character we show on the map for this item.
    /** Display / identity symbol that matches your grid legend. */
    public abstract char symbol();

    // Points this item gives or takes. Positive adds, negative subtracts.
    /** Positive values add to score, negative subtract (punishment). */
    public abstract int value();

    // If true, you must collect this to win the level. Default is false.
    /** Whether this item is required to win (i.e., regular reward). */
    public boolean isRequiredToWin() { return false; }
}
