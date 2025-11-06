package com.project.team6.enemy;



/**
 * Placeholder SimpleChaserEnemy.
 * TODO: Replace with real chase behavior.
 * Current behavior: NO-OP (does not move).
 */
public class MovingEnemy extends Enemy {

    public MovingEnemy(int x, int y) {
        super(x, y);
    }

    @Override
    public void tick(char[][] grid, int playerX, int playerY) {
        // Intentionally left blank so teammates can implement.
        // Keep signature stable so the game loop can call tick safely.
    }
}

