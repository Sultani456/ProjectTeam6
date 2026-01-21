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
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enemy placement on a board created by BoardGenerator.
 */
final class SpawnerEnemyTest {

    @Test
    void gatesStayClearAndPathIsReachable() {
        int rows = 11;
        int cols = 18;

        GameConfig.setBoardDimensions(rows, cols);

        BoardGenerator gen = new BoardGenerator();
        BarrierOptions opts = new BarrierOptions(BarrierMode.NONE);
        BoardGenerator.Output out = gen.generate(opts);
        Board board = new Board(out);

        GameConfig.numEnemies = 10;
        GameConfig.enemyMovePeriod = 5;

        Spawner spawner = Spawner.withSeed(board, 20L);
        spawner.spawnEnemies();

        Position start = board.start();
        Position exit  = board.exit();

        Position startFront = new Position(Math.min(start.column() + 1, cols - 1), start.row());
        Position exitFront  = new Position(Math.max(exit.column() - 1, 0),        exit.row());

        assertFalse(board.cellAt(startFront).hasEnemy(), "Enemy on start front tile");
        assertFalse(board.cellAt(exitFront).hasEnemy(), "Enemy on exit front tile");

        Set<Position> blocked = new HashSet<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Position p = new Position(col, row);
                if (board.cellAt(p).hasEnemy()) {
                    blocked.add(p);
                }
            }
        }

        assertTrue(reachable(board, start, exit, blocked),
                "Enemies must not block the path from Start to Exit");
    }

    /**
     * Simple reachability check that treats blocked cells as walls.
     */
    private static boolean reachable(Board board, Position from, Position to, Set<Position> blocked) {
        if (from.equals(to)) {
            return true;
        }

        boolean[][] seen = new boolean[board.rows()][board.cols()];
        Deque<Position> q = new ArrayDeque<>();
        q.add(from);
        seen[from.row()][from.column()] = true;

        int[][] dirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(to)) {
                return true;
            }

            for (int[] d : dirs) {
                int nx = p.column() + d[0];
                int ny = p.row() + d[1];

                if (nx < 0 || nx >= board.cols() || ny < 0 || ny >= board.rows()) {
                    continue;
                }
                if (seen[ny][nx]) {
                    continue;
                }

                Position n = new Position(nx, ny);
                if (blocked != null && blocked.contains(n)) {
                    continue;
                }

                Cell c = board.cellAt(n);
                if (!c.isWalkableTerrain()) {
                    continue;
                }

                seen[ny][nx] = true;
                q.addLast(n);
            }
        }
        return false;
    }
}
