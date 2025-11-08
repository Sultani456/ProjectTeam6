package com.project.team6.model.characters.enemies;

import java.util.ArrayList;
import java.util.List;

public class EnemyFactory {

    // This class makes enemies objects for the game.
    // It gives us a list of enemies based on the grid or level data.

    /** Convert 'B' markers in the grid to Enemy objects and clear those cells. */
    public static List<Enemy> fromGridAndClear(char[][] grid) {
        // We will collect enemies here.
        List<Enemy> list = new ArrayList<>();

        // Get how many rows and columns the grid has.
        int rows = grid.length, cols = grid[0].length;

        // Go through every cell in the grid.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                // If we see a 'B', that means there should be a chaser enemies here.
                if (grid[r][c] == 'B') {
                    // Make a new enemies at this column and row.
                    list.add(new MovingEnemy(c, r));

                    // Remove the marker so the grid is clean after spawning.
                    grid[r][c] = ' '; // clear marker
                }
            }
        }

        // Give back all enemies we created.
        return list;
    }

    // For future: choose enemies subclass by level.
    public static List<Enemy> forLevel(int level, List<int[]> spawns) {
        // Right now we just make chaser enemies for each spawn point.
        List<Enemy> list = new ArrayList<>();

        // Each spawn has x at index 0 and y at index 1.
        for (int[] p : spawns) list.add(new MovingEnemy(p[0], p[1]));

        // Later we can use the level value to pick different enemies types.
        return list;
    }
}
