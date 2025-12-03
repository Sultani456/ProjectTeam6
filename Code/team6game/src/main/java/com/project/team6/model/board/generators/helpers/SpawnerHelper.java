package com.project.team6.model.board.generators.helpers;

import com.project.team6.model.board.*;

import java.util.*;

public final class SpawnerHelper {

    private SpawnerHelper() { }

    public static List<Position> freeFloorCells(Board board) {
        int rows = board.rows();
        int cols = board.cols();
        Cell[][] grid = board.grid();

        List<Position> free = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < cols; column++) {
                Cell c = grid[row][column];
                if (c.terrain() == Cell.Terrain.FLOOR &&
                        c.item() == null &&
                        !c.hasPlayer() &&
                        !c.hasEnemy()) {
                    free.add(new Position(column, row));
                }
            }
        }
        return free;
    }

    public static boolean canReach(Board board, Position from,
                                   Position to,
                                   Set<Position> blocked) {
        if (from.equals(to)) return true;

        int rows = board.rows();
        int cols = board.cols();
        Cell[][] grid = board.grid();

        boolean[][] visited = new boolean[rows][cols];
        boolean[][] blockedGrid = toBlockedGrid(blocked, rows, cols);

        Deque<Position> q = new ArrayDeque<>();
        q.add(from);
        visited[from.row()][from.column()] = true;

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(to)) return true;

            int column = p.column(), row = p.row();
            tryVisit(grid, cols, rows, column + 1, row, blockedGrid, visited, q);
            tryVisit(grid, cols, rows, column - 1, row, blockedGrid, visited, q);
            tryVisit(grid, cols, rows, column, row + 1, blockedGrid, visited, q);
            tryVisit(grid, cols, rows, column, row - 1, blockedGrid, visited, q);
        }

        return false;
    }

    private static boolean[][] toBlockedGrid(Set<Position> blocked, int rows, int cols) {
        if (blocked == null || blocked.isEmpty()) return null;

        boolean[][] blockedGrid = new boolean[rows][cols];
        for (Position p : blocked) {
            int column = p.column();
            int row = p.row();
            if (column < 0 || column >= cols || row < 0 || row >= rows) continue;
            blockedGrid[row][column] = true;
        }
        return blockedGrid;
    }

    private static void tryVisit(Cell[][] grid, int cols, int rows,
                                 int column, int row,
                                 boolean[][] blockedGrid,
                                 boolean[][] visited,
                                 Deque<Position> q) {

        if (column < 0 || column >= cols || row < 0 || row >= rows) return;
        if (visited[row][column]) return;
        if (blockedGrid != null && blockedGrid[row][column]) return;

        Cell c = grid[row][column];
        if (!c.isWalkableTerrain()) return;

        visited[row][column] = true;
        q.addLast(new Position(column, row));
    }
}
