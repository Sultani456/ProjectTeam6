package com.project.team6;


public class Player {

    public static final char PLAYER_CHAR = 'S';

    private int x;
    private int y;

    public enum MoveType {
        MOVED,
        BLOCKED,
        COLLECTED_REQUIRED,
        COLLECTED_OPTIONAL,
        HIT_PUNISHMENT,
        REACHED_EXIT,
        HIT_ENEMY
    }

    public static final class MoveResult {
        public final MoveType type;
        public final int fromX, fromY;
        public final int toX, toY;
        public final char target;

        private MoveResult(MoveType type, int fromX, int fromY, int toX, int toY, char target) {
            this.type = type;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.target = target;
        }
    }

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public static Player fromGrid(char[][] grid) {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == PLAYER_CHAR) return new Player(c, r);
            }
        }
        throw new IllegalStateException("Player start 'S' not found in grid.");
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public MoveResult tryMove(int dx, int dy, char[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        int nx = x + dx, ny = y + dy;

        if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) {
            return new MoveResult(MoveType.BLOCKED, x, y, x, y, '#');
        }
        char target = grid[ny][nx];
        if (target == 'X') {
            return new MoveResult(MoveType.BLOCKED, x, y, x, y, target);
        }

        // move
        grid[y][x] = ' ';
        grid[ny][nx] = PLAYER_CHAR;

        int oldX = x, oldY = y;
        x = nx; y = ny;

        MoveType type;
        switch (target) {
            case '.': type = MoveType.COLLECTED_REQUIRED; break;
            case 'o': type = MoveType.COLLECTED_OPTIONAL; break;
            case '*': type = MoveType.HIT_PUNISHMENT;     break;
            case 'B': type = MoveType.HIT_ENEMY;          break;
            case 'E': type = MoveType.REACHED_EXIT;       break;
            case ' ': default: type = MoveType.MOVED;     break;
        }
        return new MoveResult(type, oldX, oldY, nx, ny, target);
    }

    public void teleportTo(int nx, int ny, char[][] grid) {
        if (inBounds(x, y, grid) && grid[y][x] == PLAYER_CHAR) grid[y][x] = ' ';
        x = nx; y = ny;
        if (inBounds(x, y, grid)) grid[y][x] = PLAYER_CHAR;
    }

    private boolean inBounds(int cx, int cy, char[][] grid) {
        return cy >= 0 && cy < grid.length && cx >= 0 && cx < grid[0].length;
    }
}
