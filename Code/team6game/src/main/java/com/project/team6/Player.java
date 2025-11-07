package com.project.team6;

public class Player {

    // The symbol we use for the player on the grid.
    public static final char PLAYER_CHAR = 'S';

    // Current position of the player in grid coordinates.
    private int x;
    private int y;

    // Different outcomes when the player tries to move.
    public enum MoveType {
        MOVED,
        BLOCKED,
        COLLECTED_REQUIRED,
        COLLECTED_OPTIONAL,
        HIT_PUNISHMENT,
        REACHED_EXIT,
        HIT_ENEMY
    }

    // This stores info about a single move attempt.
    public static final class MoveResult {
        public final MoveType type;   // what happened
        public final int fromX, fromY; // start position
        public final int toX, toY;     // end position
        public final char target;      // what was on the tile

        private MoveResult(MoveType type, int fromX, int fromY, int toX, int toY, char target) {
            this.type = type;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.target = target;
        }
    }

    // Create a player at a starting position.
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    // Find 'S' in the grid and make a player there.
    public static Player fromGrid(char[][] grid) {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == PLAYER_CHAR) return new Player(c, r);
            }
        }
        throw new IllegalStateException("Player start 'S' not found in grid.");
    }

    // Give current x.
    public int getX() { return x; }
    // Give current y.
    public int getY() { return y; }

    // Try to move by (dx, dy). Returns what happened.
    public MoveResult tryMove(int dx, int dy, char[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        int nx = x + dx, ny = y + dy;

        // Stop if the move goes outside the grid.
        if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) {
            return new MoveResult(MoveType.BLOCKED, x, y, x, y, '#');
        }
        char target = grid[ny][nx];
        // Stop if the next tile is a wall.
        if (target == 'X') {
            return new MoveResult(MoveType.BLOCKED, x, y, x, y, target);
        }

        // Update the grid to move the player.
        grid[y][x] = ' ';
        grid[ny][nx] = PLAYER_CHAR;

        int oldX = x, oldY = y;
        x = nx; y = ny;

        // Decide what kind of move it was based on the target tile.
        MoveType type;
        switch (target) {
            case '.': type = MoveType.COLLECTED_REQUIRED; break; // picked a required item
            case 'o': type = MoveType.COLLECTED_OPTIONAL; break; // picked a bonus item
            case '*': type = MoveType.HIT_PUNISHMENT;     break; // stepped on a trap
            case 'B': type = MoveType.HIT_ENEMY;          break; // ran into an enemy
            case 'E': type = MoveType.REACHED_EXIT;       break; // reached the exit
            case ' ': default: type = MoveType.MOVED;     break; // just moved to empty
        }
        return new MoveResult(type, oldX, oldY, nx, ny, target);
    }

    // Move the player instantly to (nx, ny).
    public void teleportTo(int nx, int ny, char[][] grid) {
        // Clear the old spot if it still shows the player.
        if (inBounds(x, y, grid) && grid[y][x] == PLAYER_CHAR) grid[y][x] = ' ';
        x = nx; y = ny;
        // Put the player symbol at the new spot if inside bounds.
        if (inBounds(x, y, grid)) grid[y][x] = PLAYER_CHAR;
    }

    // Check if (cx, cy) is inside the grid.
    private boolean inBounds(int cx, int cy, char[][] grid) {
        return cy >= 0 && cy < grid.length && cx >= 0 && cx < grid[0].length;
    }
}
