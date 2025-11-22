package com.project.team6.model.board.generators;

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
 * Enemy placement:
 * - no enemy on the first interior tiles in front of Start or Exit
 * - Start can still reach Exit after placement
 */
final class SpawnerEnemyTest {

    @Test
    void gatesStayClearAndPathIsReachable() {
        int rows = 11, cols = 18;

        BoardGenerator gen = new BoardGenerator();
        BoardGenerator.Output out = gen.generate(
                new BarrierOptions(rows, cols, BarrierMode.NONE, null, null),
                0.0
        );
        Board board = new Board(out);

        Spawner spawner = new Spawner(board, 120);
        spawner.spawnEnemies(10, 5);

        Position s = board.start();
        Position e = board.exit();

        // first interior tiles just inside the gates
        Position sFront = new Position(Math.min(s.x() + 1, cols - 1), s.y());
        Position eFront = new Position(Math.max(e.x() - 1, 0),        e.y());

        assertFalse(board.cellAt(sFront).hasEnemy(), "Enemy on start front tile");
        assertFalse(board.cellAt(eFront).hasEnemy(), "Enemy on exit front tile");

        // treat enemy cells as blocked and ensure S -> E is still reachable
        Set<Position> blocked = new HashSet<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Position p = new Position(x, y);
                if (board.cellAt(p).hasEnemy()) blocked.add(p);
            }
        }
        assertTrue(reachable(board, s, e, blocked), "Enemies must not block the path from Start to Exit");
    }

    /** Simple BFS reachability that treats 'blocked' as impassable. */
    private static boolean reachable(Board b, Position from, Position to, Set<Position> blocked) {
        if (from.equals(to)) return true;

        boolean[][] seen = new boolean[b.rows()][b.cols()];
        Deque<Position> q = new ArrayDeque<>();
        q.add(from);
        seen[from.y()][from.x()] = true;

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(to)) return true;

            for (Position n : p.neighbors4()) {
                if (n.x() < 0 || n.x() >= b.cols() || n.y() < 0 || n.y() >= b.rows()) continue;
                if (seen[n.y()][n.x()]) continue;
                if (blocked != null && blocked.contains(n)) continue;

                Cell c = b.cellAt(n);
                if (!c.isWalkableTerrain()) continue;

                seen[n.y()][n.x()] = true;
                q.addLast(n);
            }
        }
        return false;
    }
}
