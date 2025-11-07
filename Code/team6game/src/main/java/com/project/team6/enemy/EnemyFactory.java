package com.project.team6.enemy;



import java.util.ArrayList;
import java.util.List;

public class EnemyFactory {

    /** Convert 'B' markers in the grid to Enemy objects and clear those cells. */
    public static List<Enemy> fromGridAndClear(char[][] grid) {
        List<Enemy> list = new ArrayList<>();
        int rows = grid.length, cols = grid[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 'B') {
                    list.add(new ChaserEnemy(c, r));
                    grid[r][c] = ' '; // clear marker
                }
            }
        }
        return list;
    }

    // For future: choose enemy subclass by level.
    public static List<Enemy> forLevel(int level, List<int[]> spawns) {
        List<Enemy> list = new ArrayList<>();
        for (int[] p : spawns) list.add(new ChaserEnemy(p[0], p[1]));
        return list;
    }
}
