package com.project.team6.model.board.generators.helpers;

import com.project.team6.model.board.*;

import java.util.*;

/**
 * Helpers for spawning items and enemies.
 * Includes search functions for free cells and path checks.
 */
public final class SpawnerHelper {

    private SpawnerHelper() { }

    /**
     * Finds all empty floor cells.
     * A cell is free when it is floor and has no item, player, or enemy.
     *
     * @param board the game board
     * @return list of positions that are free
     */
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

    /**
     * Checks if there is a path from one cell to another using BFS.
     * Treats positions in {@code blocked} as closed.
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
        visited[from.row()][from.column()] = true;

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(to)) return true;

            int column = p.column(), row = p.row();
            tryVisit(board, column + 1, row, blocked, visited, q);
            tryVisit(board, column - 1, row, blocked, visited, q);
            tryVisit(board, column, row + 1, blocked, visited, q);
            tryVisit(board, column, row - 1, blocked, visited, q);
        }

        return false;
    }

    /**
     * Tries to add a neighbor to the BFS queue.
     * Skips out of bounds cells, visited cells, blocked cells, and walls.
     */
    public static void tryVisit(Board board, int column, int row,
                                Set<Position> blocked,
                                boolean[][] visited,
                                Deque<Position> q) {

        int rows = board.rows();
        int cols = board.cols();
        Cell[][] grid = board.grid();

        if (column < 0 || column >= cols || row < 0 || row >= rows) return;
        if (visited[row][column]) return;

        Position p = new Position(column, row);
        if (blocked != null && blocked.contains(p)) return;

        Cell c = grid[row][column];
        if (!c.isWalkableTerrain()) return;

        visited[row][column] = true;
        q.addLast(p);
    }
}
