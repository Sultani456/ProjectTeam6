package com.project.team6;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a level from an ASCII .txt file into a char[][] grid.
 * Legend:
 *   'X' = wall
 *    ' ' = floor
 *   '.' = required reward
 *   'o' = optional reward
 *   '*' = punishment
 *   'B' = enemy spawn marker (your EnemyFactory will convert to objects)
 *   'S' = player start (optional in file; will be injected if missing)
 *   'E' = exit         (optional in file; will be injected if missing)
 *
 * Usage in GamePanel:
 *   MapLoader.LoadedLevel L = MapLoader.load("maps/level1.txt", 18, 11, true);
 *   char[][] grid = L.grid;
 *   // player is where 'S' is; exit where 'E' is
 *   requiredLeft = L.requiredCount;
 *   enemies = EnemyFactory.fromGridAndClear(grid); // converts 'B' to objects
 */
public final class MapLoader {

    private MapLoader() {}

    /** Immutable result of loading a map. */
    public static final class LoadedLevel {
        public final char[][] grid;
        public final int cols;
        public final int rows;
        public final int playerX, playerY; // -1 if not present & not injected
        public final int exitX, exitY;     // -1 if not present & not injected
        public final int requiredCount;

        LoadedLevel(char[][] grid, int playerX, int playerY, int exitX, int exitY, int requiredCount) {
            this.grid = grid;
            this.rows = grid.length;
            this.cols = grid[0].length;
            this.playerX = playerX;
            this.playerY = playerY;
            this.exitX = exitX;
            this.exitY = exitY;
            this.requiredCount = requiredCount;
        }
    }

    /**
     * Load a level from a text file.
     *
     * @param filePath       path to the ASCII map file (e.g., "maps/level1.txt")
     * @param expectedCols   required number of columns (e.g., 18)
     * @param expectedRows   required number of rows (e.g., 11)
     * @param injectFixedSE  if true and S/E are missing, inject S at (0,6) and E at (17,6)
     */
    public static LoadedLevel load(String filePath, int expectedCols, int expectedRows, boolean injectFixedSE) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8);

        // Trim trailing empty lines (common in text editors)
        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        if (lines.size() != expectedRows) {
            throw new IllegalArgumentException("Map rows mismatch: expected " + expectedRows + " but file has " + lines.size());
        }

        char[][] grid = new char[expectedRows][expectedCols];

        int playerX = -1, playerY = -1;
        int exitX   = -1, exitY   = -1;
        int requiredCount = 0;

        for (int r = 0; r < expectedRows; r++) {
            String row = lines.get(r);
            if (row.length() != expectedCols) {
                throw new IllegalArgumentException("Row " + r + " col mismatch: expected " + expectedCols + " but got " + row.length());
            }

            for (int c = 0; c < expectedCols; c++) {
                char ch = row.charAt(c);

                // Normalize tabs just in case (treat as space)
                if (ch == '\t') ch = ' ';

                // Validate characters (allow listed ones only)
                if (!isValidChar(ch)) {
                    throw new IllegalArgumentException("Invalid symbol '" + ch + "' at (" + c + "," + r + ")");
                }

                grid[r][c] = ch;

                switch (ch) {
                    case 'S':
                        if (playerX != -1)
                            throw new IllegalArgumentException("Multiple 'S' found; only one start is allowed.");
                        playerX = c; playerY = r;
                        break;
                    case 'E':
                        if (exitX != -1)
                            throw new IllegalArgumentException("Multiple 'E' found; only one exit is allowed.");
                        exitX = c; exitY = r;
                        break;
                    case '.':
                        requiredCount++;
                        break;
                    default:
                        // other cells handled as needed
                        break;
                }
            }
        }

        // Optionally inject S/E at fixed positions if missing
        if (injectFixedSE) {
            // fixed coordinates you’re using in your game:
            int fixedSX = 0,  fixedSY = 6;
            int fixedEX = expectedCols - 1, fixedEY = 6; // (17,6) for 18x11

            if (playerX == -1 || playerY == -1) {
                // "Punch" doorway if needed
                if (grid[fixedSY][fixedSX] == 'X') grid[fixedSY][fixedSX] = ' ';
                // Ensure adjacent interior is open, so player can enter
                if (fixedSX + 1 < expectedCols && grid[fixedSY][fixedSX + 1] == 'X') grid[fixedSY][fixedSX + 1] = ' ';
                grid[fixedSY][fixedSX] = 'S';
                playerX = fixedSX; playerY = fixedSY;
            }

            if (exitX == -1 || exitY == -1) {
                if (grid[fixedEY][fixedEX] == 'X') grid[fixedEY][fixedEX] = ' ';
                if (fixedEX - 1 >= 0 && grid[fixedEY][fixedEX - 1] == 'X') grid[fixedEY][fixedEX - 1] = ' ';
                grid[fixedEY][fixedEX] = 'E';
                exitX = fixedEX; exitY = fixedEY;
            }
        }

        return new LoadedLevel(grid, playerX, playerY, exitX, exitY, requiredCount);
    }

    private static boolean isValidChar(char ch) {
        return ch == 'X' || ch == ' ' || ch == '.' || ch == 'o' || ch == '*' || ch == 'B' || ch == 'S' || ch == 'E';
    }
}

