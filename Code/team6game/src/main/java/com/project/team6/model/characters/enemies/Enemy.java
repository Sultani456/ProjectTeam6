package com.project.team6.model.characters.enemies;

import com.project.team6.model.characters.CharacterObject;

// This is the base class for enemies.
// Other enemy types will extend this class.
public abstract class Enemy extends CharacterObject {
    public Enemy(int x, int y) {
        super(x, y);
    }

    /** Move at most one tile. */
    // This runs every game tick to decide the next move.
    // The grid tells where walls are. Player position is given.
    public abstract void tick(char[][] grid, int playerX, int playerY);
}
