package com.project.team6.ui;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * Loads a level from an ASCII .txt file into a char[][] grid.
 * Legend:
 *   'X' = wall
 *    ' ' = floor
 *   '.' = required reward
 *   'o' = optional reward
 *   '*' = punishment
 *   'B' = enemies spawn marker
 *   'S' = player start (optional; will be injected if missing)
 *   'E' = exit         (optional; will be injected if missing)
 *
 * Usage in GamePanel:
 *   MapLoader.LoadedLevel L = MapLoader.load("maps/level1.txt", 18, 11, true);
 *   char[][] grid = L.grid;
 *   requiredLeft = L.requiredCount;
 */
public final class MapLoader {

    private MapLoader() {} // I make the class non-instantiable. It is just a helper.

    /** Immutable result of loading a map. */
    public static final class LoadedLevel {
        public final char[][] grid;   // the map as characters
        public final int cols;        // number of columns
        public final int rows;        // number of rows
        public final int playerX, playerY; // start position if it exists
        public final int exitX, exitY;     // exit position if it exists
        public final int requiredCount;    // how many '.' on the map

        LoadedLevel(char[][] grid, int playerX, int playerY, int exitX, int exitY, int requiredCount) {
            this.grid = grid;
            this.rows = grid.length;       // rows come from the array height
            this.cols = grid[0].length;    // cols come from the first row
            this.playerX = playerX;
            this.playerY = playerY;
            this.exitX = exitX;
            this.exitY = exitY;
            this.requiredCount = requiredCount;
        }
    }

    /**
     * Load a level from a text file (from the classpath).
     *
     * @param filePath       classpath-relative path (e.g., "maps/level1.txt")
     * @param expectedCols   required number of columns (e.g., 18)
     * @param expectedRows   required number of rows (e.g., 11)
     * @param injectFixedSE  if true and S/E are missing, inject S at (0,6) and E at (expectedCols-1,6)
     */
    public static LoadedLevel load(String filePath, int expectedCols, int expectedRows, boolean injectFixedSE) throws IOException {
        // I normalize the path so it works from resources.
        String resourcePath = "/" + filePath.replace('\\', '/');

        List<String> lines = new ArrayList<>();
        // I try to open the file from the classpath.
        try (InputStream in = MapLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + resourcePath); // file not found
            }
            // I read the file line by line using UTF-8.
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line); // save each row of the map
                }
            }
        }

        // Some editors add empty lines at the end. I remove them.
        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        // The file must have the expected number of rows.
        if (lines.size() != expectedRows) {
            throw new IllegalArgumentException("Map rows mismatch: expected " + expectedRows + " but file has " + lines.size());
        }

        char[][] grid = new char[expectedRows][expectedCols]; // final grid buffer

        int playerX = -1, playerY = -1; // default means “not found”
        int exitX   = -1, exitY   = -1;
        int requiredCount = 0;          // count of required rewards

        // I go over each row of the text map.
        for (int r = 0; r < expectedRows; r++) {
            String row = lines.get(r);
            // Each row must match the expected width.
            if (row.length() != expectedCols) {
                throw new IllegalArgumentException("Row " + r + " col mismatch: expected " + expectedCols + " but got " + row.length());
            }

            // I scan each character in the row.
            for (int c = 0; c < expectedCols; c++) {
                char ch = row.charAt(c);

                // Tabs can break the layout. I treat them as spaces.
                if (ch == '\t') ch = ' ';

                // Only allow symbols from the legend.
                if (!isValidChar(ch)) {
                    throw new IllegalArgumentException("Invalid symbol '" + ch + "' at (" + c + "," + r + ")");
                }

                grid[r][c] = ch; // write into the grid

                // I track special cells while reading.
                switch (ch) {
                    case 'S':
                        if (playerX != -1) {
                            throw new IllegalArgumentException("Multiple 'S' found; only one start is allowed."); // only one start
                        }
                        playerX = c; playerY = r; // remember start
                        break;
                    case 'E':
                        if (exitX != -1) {
                            throw new IllegalArgumentException("Multiple 'E' found; only one exit is allowed."); // only one exit
                        }
                        exitX = c; exitY = r; // remember exit
                        break;
                    case '.':
                        requiredCount++; // count required rewards
                        break;
                    default:
                        // other cells are fine as-is
                        break;
                }
            }
        }

        // If asked, I place S/E at fixed spots when they are missing.
        if (injectFixedSE) {
            int fixedSX = 0, fixedSY = 6; // left side middle row
            int fixedEX = expectedCols - 1, fixedEY = 6; // right side middle row

            // If start is missing, I carve a small doorway and set S.
            if (playerX == -1 || playerY == -1) {
                if (grid[fixedSY][fixedSX] == 'X') grid[fixedSY][fixedSX] = ' '; // open the tile
                if (fixedSX + 1 < expectedCols && grid[fixedSY][fixedSX + 1] == 'X') grid[fixedSY][fixedSX + 1] = ' '; // make space next to it
                grid[fixedSY][fixedSX] = 'S';
                playerX = fixedSX; playerY = fixedSY; // save the injected position
            }

            // If exit is missing, I do the same on the right side.
            if (exitX == -1 || exitY == -1) {
                if (grid[fixedEY][fixedEX] == 'X') grid[fixedEY][fixedEX] = ' ';
                if (fixedEX - 1 >= 0 && grid[fixedEY][fixedEX - 1] == 'X') grid[fixedEY][fixedEX - 1] = ' ';
                grid[fixedEY][fixedEX] = 'E';
                exitX = fixedEX; exitY = fixedEY;
            }
        }

        // I return all the data together.
        return new LoadedLevel(grid, playerX, playerY, exitX, exitY, requiredCount);
    }

    // This checks if a map symbol is allowed.
    private static boolean isValidChar(char ch) {
        return ch == 'X' || ch == ' ' || ch == '.' || ch == 'o' || ch == '*' || ch == 'B' || ch == 'S' || ch == 'E';
    }
}
