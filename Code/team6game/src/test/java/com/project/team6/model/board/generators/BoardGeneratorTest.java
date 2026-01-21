package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.barrierProperties.BarrierMode;
import com.project.team6.model.board.generators.barrierProperties.BarrierOptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that random boards have a path from start to exit.
 */
final class BoardGeneratorTest {

    /**
     * Random mode should always give a path from start to exit.
     * Uses a smaller board so the test runs fast.
     */
    @Test
    void randomHasPathFromStartToExit() {
        int oldRows = GameConfig.rows;
        int oldCols = GameConfig.cols;
        double oldDensity = GameConfig.boardBarrierPercentage;

        try {
            // Use a smaller board and current barrier percentage.
            GameConfig.setBoardDimensions(10, 10);
            GameConfig.boardBarrierPercentage = 0.25;

            BoardGenerator gen = new BoardGenerator();
            BarrierOptions opts = new BarrierOptions(BarrierMode.RANDOM);
            BoardGenerator.Output out = gen.generate(opts);
            Board board = new Board(out);

            assertTrue(
                    reachable(board, board.start(), board.exit()),
                    "Start should reach exit in generated boards"
            );
        } finally {
            // Restore global config for other tests.
            GameConfig.setBoardDimensions(oldRows, oldCols);
            GameConfig.boardBarrierPercentage = oldDensity;
        }
    }

    /**
     * Simple BFS that checks if one cell can reach another.
     */
    private boolean reachable(Board board, Position from, Position to) {
        if (from.equals(to)) {
            return true;
        }

        boolean[][] seen = new boolean[board.rows()][board.cols()];
        Deque<Position> queue = new ArrayDeque<>();

        seen[from.row()][from.column()] = true;
        queue.addLast(from);

        while (!queue.isEmpty()) {
            Position p = queue.removeFirst();
            if (p.equals(to)) {
                return true;
            }

            tryVisit(board, p.column() + 1, p.row(), seen, queue);
            tryVisit(board, p.column() - 1, p.row(), seen, queue);
            tryVisit(board, p.column(), p.row() + 1, seen, queue);
            tryVisit(board, p.column(), p.row() - 1, seen, queue);
        }

        return false;
    }

    /**
     * Adds a neighbor to the BFS queue when it is valid and walkable.
     */
    private void tryVisit(Board board,
                          int x,
                          int y,
                          boolean[][] seen,
                          Deque<Position> queue) {
        if (x < 0 || x >= board.cols() || y < 0 || y >= board.rows()) {
            return;
        }
        if (seen[y][x]) {
            return;
        }

        Position pos = new Position(x, y);
        Cell cell = board.cellAt(pos);
        if (!cell.isWalkableTerrain()) {
            return;
        }

        seen[y][x] = true;
        queue.addLast(pos);
    }
}
