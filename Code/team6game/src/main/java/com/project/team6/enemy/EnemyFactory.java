package com.project.team6.enemy;



import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder factory: preserves API and clears grid spawn markers,
 * but returns an empty enemy list so the game remains playable.
 * TODO: Instantiate real enemies here later.
 */
public class EnemyFactory {

    /** Convert 'B' markers to enemies; placeholder clears markers and returns empty list. */
    public static List<Enemy> fromGridAndClear(char[][] grid) {
        List<Enemy> list = new ArrayList<>();
        if (grid == null || grid.length == 0) return list;

        int rows = grid.length, cols = grid[0].length;
        int found = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 'B') {
                    // Placeholder choice: clear the marker so the map is clean,
                    // but DO NOT spawn yet (leave that to teammate).
                    grid[r][c] = ' ';
                    found++;
                }
            }
        }

        if (found > 0) {
            System.out.println("[EnemyFactory] Placeholder cleared " + found + " spawn marker(s).");
            System.out.println("[EnemyFactory] TODO: spawn SimpleChaserEnemy instances at those positions.");
        }
        return list; // intentionally empty for now
    }

    /** Placeholder: create enemies from a spawn list; returns empty until implemented. */
    public static List<Enemy> forLevel(int level, List<int[]> spawns) {
        System.out.println("[EnemyFactory] Placeholder forLevel(level=" + level + ") called with "
                + (spawns == null ? 0 : spawns.size()) + " spawn(s).");
        System.out.println("[EnemyFactory] TODO: instantiate enemies based on level and spawns.");
        return new ArrayList<>();
    }
}

