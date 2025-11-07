package com.project.team6.enemy;

// This is the base class for enemies.
// Other enemy types will extend this class.
public abstract class Enemy {
    // Current x position on the grid.
    protected int x;
    // Current y position on the grid.
    protected int y;

    // Set the starting position.
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Get the x position. It is read-only here.
    public final int getX() { return x; }
    // Get the y position. It is read-only here.
    public final int getY() { return y; }

    /** Move at most one tile. */
    // This runs every game tick to decide the next move.
    // The grid tells where walls are. Player position is given.
    public abstract void tick(char[][] grid, int playerX, int playerY);

    // Check if this enemy stands on the given grid cell.
    public final boolean occupies(int gx, int gy) {
        return this.x == gx && this.y == gy;
    }

    // Check if the enemy can step to (nx, ny).
    // It must be inside the grid and not a wall.
    // Walls are marked with 'X'.
    protected boolean canStep(char[][] grid, int nx, int ny) {
        if (ny < 0 || ny >= grid.length) return false;
        if (nx < 0 || nx >= grid[0].length) return false;
        return grid[ny][nx] != 'X';
    }
}
