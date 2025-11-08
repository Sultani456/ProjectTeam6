package com.project.team6.model;

// This class is the parent for all items a player can step on.
// Other item types will extend this class.

/** Base type for anything the player can step on and trigger. */
public abstract class GameObject {
    // Grid x position for this item.
    protected int x;
    // Grid y position for this item.
    protected int y;

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
