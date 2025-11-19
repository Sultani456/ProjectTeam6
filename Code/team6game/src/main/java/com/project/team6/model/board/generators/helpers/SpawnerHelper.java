package com.project.team6.model.board.generators.helpers;

import com.project.team6.model.board.*;

import java.util.*;

public final class SpawnerHelper {

    // -----------------------------------------------------------------
    // Reachability Functions
    // -----------------------------------------------------------------

    /** All empty floor cells (no items, no occupants). */
    public static List<Position> freeFloorCells(Board board) {
        int rows = board.rows();
        int cols = board.cols();
        Cell[][] grid = board.grid();

        List<Position> free = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Position p = new Position(x, y);
                Cell c = grid[y][x];
                if (c.terrain() == Cell.Terrain.FLOOR &&
                        c.item() == null &&
                        !c.hasPlayer() &&
                        !c.hasEnemy()) {
                    free.add(p);
                }
            }
        }
        return free;
    }

    /**
     * BFS reachability helper used by Spawner to ensure punishments don’t
     * block required paths.
     */
    public static boolean canReach(Board board, Position from,
                            Position to,
                            Set<Position> blocked) {
        if (from.equals(to)) return true;

        int rows = board.rows();
        int cols = board.cols();

        boolean[][] visited = new boolean[rows][cols];
        Deque<Position> q = new ArrayDeque<>();
        q.add(from);
        visited[from.y()][from.x()] = true;

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(to)) return true;

            int x = p.x(), y = p.y();
            tryVisit(board, x + 1, y, blocked, visited, q);
            tryVisit(board, x - 1, y, blocked, visited, q);
            tryVisit(board, x, y + 1, blocked, visited, q);
            tryVisit(board, x, y - 1, blocked, visited, q);
        }

        return false;
    }

    public static void tryVisit(Board board, int x, int y,
                         Set<Position> blocked,
                         boolean[][] visited,
                         Deque<Position> q) {

        int rows = board.rows();
        int cols = board.cols();
        Cell[][] grid = board.grid();

        if (x < 0 || x >= cols || y < 0 || y >= rows) return;
        if (visited[y][x]) return;

        Position p = new Position(x, y);
        if (blocked != null && blocked.contains(p)) return;

        Cell c = grid[y][x];
        if (!c.isWalkableTerrain()) return;

        visited[y][x] = true;
        q.addLast(p);
    }
}

