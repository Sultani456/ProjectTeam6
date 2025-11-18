package com.project.team6.model.boardUtilities.generators.helpers;

import com.project.team6.model.boardUtilities.Cell;
import com.project.team6.model.boardUtilities.Position;
import com.project.team6.model.boardUtilities.generators.BoardGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GeneratorHelper {
    // --------------------------------------------------------------------
    // BFS helpers for validation
    // --------------------------------------------------------------------

    public static boolean isPassable(Cell.Terrain t) {
        return switch (t) {
            case WALL, BARRIER -> false;
            default -> true;   // FLOOR, START, EXIT
        };
    }

    public static int BFSCount(Cell.Terrain[][] terrain,
                                Position start,
                                boolean[][] visited) {
        int rows = terrain.length;
        int cols = terrain[0].length;

        Deque<Position> q = new ArrayDeque<>();
        q.addLast(start);
        visited[start.y()][start.x()] = true;

        int count = 0;
        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            count++;

            int x = p.x();
            int y = p.y();

            tryVisit(terrain, visited, q, x + 1, y);
            tryVisit(terrain, visited, q, x - 1, y);
            tryVisit(terrain, visited, q, x, y + 1);
            tryVisit(terrain, visited, q, x, y - 1);
        }
        return count;
    }

    public static void tryVisit(Cell.Terrain[][] terrain,
                                 boolean[][] visited,
                                 Deque<Position> q,
                                 int x, int y) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        if (x < 0 || x >= cols || y < 0 || y >= rows) return;
        if (visited[y][x]) return;
        if (!isPassable(terrain[y][x])) return;

        visited[y][x] = true;
        q.addLast(new Position(x, y));
    }

    public static int countPassable(Cell.Terrain[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        int count = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (isPassable(terrain[y][x])) count++;
            }
        }
        return count;
    }

    public static boolean hasIsolatedFloor(Cell.Terrain[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (!isPassable(terrain[y][x])) continue;
                int open = 0;
                if (y > 0     && isPassable(terrain[y - 1][x])) open++;
                if (y < rows - 1 && isPassable(terrain[y + 1][x])) open++;
                if (x > 0     && isPassable(terrain[y][x - 1])) open++;
                if (x < cols - 1 && isPassable(terrain[y][x + 1])) open++;
                if (open == 0) return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------
    // Validation for RANDOM configuration
    // --------------------------------------------------------------------

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
        if (!visited[exit.y()][exit.x()]) {
            return false;
        }

        // All passable cells must be in one connected region
        int totalPassable = countPassable(terrain);
        if (reachable < totalPassable) {
            return false;
        }

        // No 1x1 prison cells
        if (hasIsolatedFloor(terrain)) {
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------
    // Utility helpers
    // --------------------------------------------------------------------

    public static void validateSize(int rows, int cols) {
        if (rows < 3 || cols < 3) {
            throw new IllegalArgumentException("rows/cols must be >= 3");
        }
    }

    public static boolean[][] perimeterWalls(int rows, int cols) {
        boolean[][] walls = new boolean[rows][cols];
        for (int y = 0; y < rows; y++) {
            walls[y][0]        = true;
            walls[y][cols - 1] = true;
        }
        for (int x = 0; x < cols; x++) {
            walls[0][x]        = true;
            walls[rows - 1][x] = true;
        }
        return walls;
    }

    public static Position randomEdgeStart(int rows, int cols, Random rng) {
        int y = 1 + rng.nextInt(rows - 2); // west edge, avoid corners
        return new Position(0, y);
    }

    public static Position randomEdgeExit(int rows, int cols, Random rng) {
        int y = 1 + rng.nextInt(rows - 2); // east edge, avoid corners
        return new Position(cols - 1, y);
    }

    /**
     * Build a terrain grid from walls + barriers + fixed START/EXIT.
     */
    public static Cell.Terrain[][] toTerrainGrid(int rows, int cols,
                                                  boolean[][] walls,
                                                  boolean[][] barriers,
                                                  Position start,
                                                  Position exit) {
        Cell.Terrain[][] t = new Cell.Terrain[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                if (start != null && start.x() == x && start.y() == y) {
                    t[y][x] = Cell.Terrain.START;
                } else if (exit != null && exit.x() == x && exit.y() == y) {
                    t[y][x] = Cell.Terrain.EXIT;
                } else if (walls[y][x]) {
                    t[y][x] = Cell.Terrain.WALL;
                } else if (barriers[y][x]) {
                    t[y][x] = Cell.Terrain.BARRIER;
                } else {
                    t[y][x] = Cell.Terrain.FLOOR;
                }
            }
        }
        return t;
    }

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
