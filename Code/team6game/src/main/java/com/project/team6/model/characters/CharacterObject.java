package com.project.team6.model.characters;

// This is the base class for all characters.
// Other CharacterObject types will extend this class
public abstract class CharacterObject {
    protected int x;
    // Current y position on the grid.
    protected int y;

    // Set the starting position.
    public CharacterObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Get the x position. It is read-only here.
    public final int getX() { return x; }
    // Get the y position. It is read-only here.
    public final int getY() { return y; }

    // Check if this character stands on the given grid cell.
    public final boolean occupies(int gx, int gy) {
        return this.x == gx && this.y == gy;
    }

    // Check if the character can step into (nx, ny).
    // It must be inside the grid and not a wall.
    // Walls are marked with 'X'.
    protected boolean canStep(char[][] grid, int nx, int ny) {
        if (nx < 0 || nx >= grid[0].length) return false;
        if (ny < 0 || ny >= grid.length) return false;
        return grid[ny][nx] != 'X';
    }

    // Check if (cx, cy) is inside the grid.
    protected boolean inBounds(int cx, int cy, char[][] grid) {
        return cy >= 0 &&
                cy < grid.length &&
                cx >= 0 &&
                cx < grid[0].length;
    }
}
