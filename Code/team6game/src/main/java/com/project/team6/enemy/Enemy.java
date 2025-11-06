package com.project.team6.enemy;



/**
 * Placeholder Enemy base class.
 * Keeps the public API intact so the game compiles.
 * TODO: Implement real enemy logic.
 */
public abstract class Enemy {
    protected int x;
    protected int y;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int getX() { return x; }
    public final int getY() { return y; }

    /** Move at most one tile (placeholder: does nothing). */
    public abstract void tick(char[][] grid, int playerX, int playerY);

    /** True if this enemy occupies (gx, gy). */
    public final boolean occupies(int gx, int gy) {
        return this.x == gx && this.y == gy;
    }

    /** Utility: in-bounds and not a wall. */
    protected boolean canStep(char[][] grid, int nx, int ny) {
        if (grid == null || grid.length == 0) return false;
        if (ny < 0 || ny >= grid.length) return false;
        if (nx < 0 || nx >= grid[0].length) return false;
        return grid[ny][nx] != 'X';
    }
}

