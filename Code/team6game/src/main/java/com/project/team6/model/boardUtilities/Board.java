package com.project.team6.model.boardUtilities;

public class Board {

    // This class holds the grid for the game.
    // It stores size, blocked cells, and start/end cells.
    private final int cols;
    private final int rows;

    // Grid data
    private final boolean[][] walls;     // true = wall (impassable)
    private final boolean[][] barriers;  // true = barrier (impassable, single-cell obstacle)

    // Start / End positions (cell coordinates)
    private int startX = 0, startY = 0;
    private int endX   = 0, endY   = 0;

    public Board(int cols, int rows) {
        // We do not allow zero or negative sizes.
        if (cols <= 0 || rows <= 0) throw new IllegalArgumentException("cols/rows must be > 0");
        this.cols = cols;
        this.rows = rows;
        // Each cell can be a wall or a barrier. Both are blocked.
        this.walls    = new boolean[rows][cols];
        this.barriers = new boolean[rows][cols];
    }

    // ------- Dimensions
    // Return number of columns.
    public int cols() { return cols; }
    // Return number of rows.
    public int rows() { return rows; }

    // ------- Bounds & queries
    // Check if (x, y) is inside the grid.
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < cols && y < rows;
    }

    // Check if a cell is a wall.
    public boolean isWall(int x, int y) {
        return inBounds(x, y) && walls[y][x];
    }

    // Check if a cell is a barrier.
    public boolean isBarrier(int x, int y) {
        return inBounds(x, y) && barriers[y][x];
    }

    /** A cell is walkable if it is in bounds and NOT a wall or barrier. */
    // This is what we use to see if the player can move to a cell.
    public boolean isWalkable(int x, int y) {
        return inBounds(x, y) && !walls[y][x] && !barriers[y][x];
    }

    // ------- Mutation helpers (used by MapLoader later)
    // Mark or unmark a wall at (x, y).
    public void setWall(int x, int y, boolean value) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        walls[y][x] = value;
    }

    // Mark or unmark a barrier at (x, y).
    public void setBarrier(int x, int y, boolean value) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        barriers[y][x] = value;
    }

    // Set the start cell for the player.
    public void setStart(int x, int y) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        startX = x; startY = y;
    }

    // Set the end cell (goal).
    public void setEnd(int x, int y) {
        if (!inBounds(x, y)) throw new IndexOutOfBoundsException();
        endX = x; endY = y;
    }

    // ------- Accessors for start/end
    // Get start x.
    public int startX() { return startX; }
    // Get start y.
    public int startY() { return startY; }
    // Get end x.
    public int endX()   { return endX; }
    // Get end y.
    public int endY()   { return endY; }

    // ------- for convenience: clear all data (It's optional)
    // Remove all walls and barriers, and reset start/end to (0,0).
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
