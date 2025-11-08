package com.project.team6.model.characters.enemies;

import com.project.team6.model.characters.CharacterObject;

// This is the base class for enemies.
// Other enemy types will extend this class.
public abstract class Enemy extends CharacterObject {
    public Enemy(int x, int y) {
        super(x, y);
    }
}
