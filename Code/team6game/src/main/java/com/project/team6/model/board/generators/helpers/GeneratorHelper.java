package com.project.team6.model.board.generators.helpers;

import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.BoardGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Helper methods for generating and validating board terrain.
 * Includes BFS utilities, size checks, wall creation, and file loading.
 */
public class GeneratorHelper {
    // --------------------------------------------------------------------
    // BFS helpers for validation
    // --------------------------------------------------------------------

    /**
     * Checks if a terrain cell can be walked on.
     *
     * @param t terrain type
     * @return true if floor, start, or exit
     */
    public static boolean isPassable(Cell.Terrain t) {
        return switch (t) {
            case WALL, BARRIER -> false;
            default -> true;   // FLOOR, START, EXIT
        };
    }

    /**
     * Runs BFS and counts how many passable cells are reachable.
     * Marks visited cells in the provided matrix.
     *
     * @param terrain terrain grid
     * @param start   starting position
     * @param visited visited flags, same size as terrain
     * @return number of reachable cells
     */
    public static int BFSCount(Cell.Terrain[][] terrain,
                                Position start,
                                boolean[][] visited) {

        Deque<Position> q = new ArrayDeque<>();
        q.addLast(start);
        visited[start.row()][start.column()] = true;

        int count = 0;
        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            count++;

            int column = p.column();
            int row = p.row();

            tryVisit(terrain, visited, q, column + 1, row);
            tryVisit(terrain, visited, q, column - 1, row);
            tryVisit(terrain, visited, q, column, row + 1);
            tryVisit(terrain, visited, q, column, row - 1);
        }
        return count;
    }

    /**
     * Adds a cell to the BFS queue if it is inside the grid, not visited, and passable.
     *
     * @param terrain terrain grid
     * @param visited visited flags
     * @param q       BFS queue
     * @param column       column index
     * @param row       row index
     */
    public static void tryVisit(Cell.Terrain[][] terrain,
                                 boolean[][] visited,
                                 Deque<Position> q,
                                 int column, int row) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        if (column < 0 || column >= cols || row < 0 || row >= rows) return;
        if (visited[row][column]) return;
        if (!isPassable(terrain[row][column])) return;

        visited[row][column] = true;
        q.addLast(new Position(column, row));
    }

    /**
     * Counts how many cells in the grid are passable.
     *
     * @param terrain terrain grid
     * @return number of passable cells
     */
    public static int countPassable(Cell.Terrain[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < cols; column++) {
                if (isPassable(terrain[row][column])) count++;
            }
        }
        return count;
    }

    /**
     * Detects if any passable cell has no passable neighbors.
     *
     * @param terrain terrain grid
     * @return true if an isolated passable cell exists
     */
    public static boolean hasIsolatedFloor(Cell.Terrain[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < cols; column++) {
                if (!isPassable(terrain[row][column])) continue;
                int open = 0;
                if (row > 0        && isPassable(terrain[row - 1][column])) open++;
                if (row < rows - 1 && isPassable(terrain[row + 1][column])) open++;
                if (column > 0        && isPassable(terrain[row][column - 1])) open++;
                if (column < cols - 1 && isPassable(terrain[row][column + 1])) open++;
                if (open == 0) return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------
    // Validation for RANDOM configuration
    // --------------------------------------------------------------------

    /**
     * Validates a barrier layout for the random mode.
     * Requires exit reachability, full connectivity of passable cells, and no isolated floor.
     *
     * @param walls    wall mask
     * @param barriers barrier mask
     * @param start    start position
     * @param exit     exit position
     * @return true if configuration is valid
     */
    public static boolean isBarrierConfigurationValid(boolean[][] walls,
                                                boolean[][] barriers,
                                                Position start,
                                                Position exit) {
        int rows = walls.length;
        int cols = walls[0].length;

        Cell.Terrain[][] terrain =
                toTerrainGrid(rows, cols, walls, barriers, start, exit);

        boolean[][] visited = new boolean[rows][cols];
        int reachable = BFSCount(terrain, start, visited);

        // Exit must be reachable
        if (!visited[exit.row()][exit.column()]) {
            return false;
        }

        // All passable cells must be connected
        int totalPassable = countPassable(terrain);
        if (reachable < totalPassable) {
            return false;
        }

        // No isolated floor cells
        if (hasIsolatedFloor(terrain)) {
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------
    // Utility helpers
    // --------------------------------------------------------------------

    /**
     * Checks that board size is at least 3 by 3.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @throws IllegalArgumentException if rows or cols is less than 3
     */
    public static void validateSize(int rows, int cols) {
        if (rows < 3 || cols < 3) {
            throw new IllegalArgumentException("rows/cols must be >= 3");
        }
    }

    /**
     * Creates a wall mask that surrounds the board.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @return boolean grid with perimeter walls set to true
     */
    public static boolean[][] perimeterWalls(int rows, int cols) {
        boolean[][] walls = new boolean[rows][cols];
        for (int row = 0; row < rows; row++) {
            walls[row][0]        = true;
            walls[row][cols - 1] = true;
        }
        for (int column = 0; column < cols; column++) {
            walls[0][column]        = true;
            walls[rows - 1][column] = true;
        }
        return walls;
    }

    /**
     * Picks a start position on the west edge, not in a corner.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param rng  random source
     * @return a start position
     */
    public static Position randomEdgeStart(int rows, int cols, Random rng) {
        int y = 1 + rng.nextInt(rows - 2); // west edge, avoid corners
        return new Position(0, y);
    }

    /**
     * Picks an exit position on the east edge, not in a corner.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param rng  random source
     * @return an exit position
     */
    public static Position randomEdgeExit(int rows, int cols, Random rng) {
        int row = 1 + rng.nextInt(rows - 2); // east edge, avoid corners
        return new Position(cols - 1, row);
    }

    /**
     * Builds a terrain grid from walls, barriers, and fixed start and exit.
     *
     * @param rows     number of rows
     * @param cols     number of columns
     * @param walls    wall mask
     * @param barriers barrier mask
     * @param start    start position, may be null
     * @param exit     exit position, may be null
     * @return terrain grid
     */
    public static Cell.Terrain[][] toTerrainGrid(int rows, int cols,
                                                  boolean[][] walls,
                                                  boolean[][] barriers,
                                                  Position start,
                                                  Position exit) {
        Cell.Terrain[][] t = new Cell.Terrain[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < cols; column++) {

                if (start != null && start.column() == column && start.row() == row) {
                    t[row][column] = Cell.Terrain.START;
                } else if (exit != null && exit.column() == column && exit.row() == row) {
                    t[row][column] = Cell.Terrain.EXIT;
                } else if (walls[row][column]) {
                    t[row][column] = Cell.Terrain.WALL;
                } else if (barriers[row][column]) {
                    t[row][column] = Cell.Terrain.BARRIER;
                } else {
                    t[row][column] = Cell.Terrain.FLOOR;
                }
            }
        }
        return t;
    }

    /**
     * Reads all lines from a classpath resource.
     *
     * @param resourcePath path relative to the classpath root
     * @return list of lines in order
     * @throws IllegalArgumentException if the resource is not found
     * @throws RuntimeException if an IO error occurs
     */
    public static List<String> readLinesFromResource(String resourcePath) {
        try (InputStream in = BoardGenerator.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource " + resourcePath, e);
        }
    }
}
