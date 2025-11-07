package com.project.team6;

public class Board {

    private final int cols;
    private final int rows;

    // Grid data
    private final boolean[][] walls;     // true = wall (impassable)
    private final boolean[][] barriers;  // true = barrier (impassable, single-cell obstacle)

    // Start / End positions (cell coordinates)
    private int startX = 0, startY = 0;
    private int endX   = 0, endY   = 0;

    public Board(int cols, int rows) {
        if (cols <= 0 || rows <= 0) throw new IllegalArgumentException("cols/rows must be > 0");
        this.cols = cols;
        this.rows = rows;
        this.walls    = new boolean[rows][cols];
        this.barriers = new boolean[rows][cols];
    }

    // ------- Dimensions
    public int cols() { return cols; }
    public int rows() { return rows; }

    // ------- Bounds & queries
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < cols && y < rows;
    }

    public boolean isWall(int x, int y) {
        return inBounds(x, y) && walls[y][x];
    }

    public boolean isBarrier(int x, int y) {
        return inBounds(x, y) && barriers[y][x];
    }

    /** A cell is walkable if it is in bounds and NOT a wall or barrier. */
    public boolean isWalkable(int x, int y) {
        return inBounds(x, y) && !walls[y][x] && !barriers[y][x];
    }

    // ------- Mutation helpers (used by MapLoader later)
    public void setWall(int x, int y, boolean value) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        walls[y][x] = value;
    }

    public void setBarrier(int x, int y, boolean value) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        barriers[y][x] = value;
    }

    public void setStart(int x, int y) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        startX = x; startY = y;
    }

    public void setEnd(int x, int y) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        endX = x; endY = y;
    }

    // ------- Accessors for start/end
    public int startX() { return startX; }
    public int startY() { return startY; }
    public int endX()   { return endX; }
    public int endY()   { return endY; }

    // ------- Convenience: clear all data (optional)
    public void clear() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                walls[y][x] = false;
                barriers[y][x] = false;
            }
        }
        startX = startY = endX = endY = 0;
    }
}

