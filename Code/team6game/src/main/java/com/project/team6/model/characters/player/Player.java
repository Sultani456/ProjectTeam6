package com.project.team6.model.characters.player;

import com.project.team6.model.characters.CharacterObject;

public class Player extends CharacterObject {

    // The symbol we use for the player on the grid.
    public static final char PLAYER_CHAR = 'S';

    // Create a player at a starting position.
    public Player(int startX, int startY) {
        super(startX, startY);
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
        MoveType type = switch (target) {
            case '.' -> MoveType.COLLECTED_REQUIRED; // picked a required item
            case 'o' -> MoveType.COLLECTED_OPTIONAL; // picked a bonus item
            case '*' -> MoveType.HIT_PUNISHMENT; // stepped on a trap
            case 'B' -> MoveType.HIT_ENEMY; // ran into an enemies
            case 'E' -> MoveType.REACHED_EXIT; // reached the exit
            default -> MoveType.MOVED; // just moved to empty
        };
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
}
