package com.project.team6.enemy;

public abstract class Enemy {
    protected int x;
    protected int y;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int getX() { return x; }
    public final int getY() { return y; }

    /** Move at most one tile. */
    public abstract void tick(char[][] grid, int playerX, int playerY);

    public final boolean occupies(int gx, int gy) {
        return this.x == gx && this.y == gy;
    }

    protected boolean canStep(char[][] grid, int nx, int ny) {
        if (ny < 0 || ny >= grid.length) return false;
        if (nx < 0 || nx >= grid[0].length) return false;
        return grid[ny][nx] != 'X';
    }
}
