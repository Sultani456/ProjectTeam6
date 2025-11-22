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
 * BoardGenerator output sanity checks.
 * Ensures a path from start to exit exists.
 */
final class BoardGeneratorTest {

    @Test
    void randomHasPathFromStartToExit() {
        int rows = 18, cols = 26;

        BoardGenerator gen = new BoardGenerator();
        BoardGenerator.Output out = gen.generate(
                new BarrierOptions(rows, cols, BarrierMode.RANDOM, null, null),
                0.25
        );
        Board board = new Board(out);

        assertTrue(reachable(board, board.start(), board.exit()),
                "Start should reach exit in generated boards");
    }

    private boolean reachable(Board b, Position from, Position to) {
        if (from.equals(to)) return true;
        Set<Position> seen = new HashSet<>();
        Deque<Position> q = new ArrayDeque<>();
        seen.add(from);
        q.add(from);

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(to)) return true;

            for (Position n : p.neighbors4()) {
                if (n.x() < 0 || n.x() >= b.cols() || n.y() < 0 || n.y() >= b.rows()) continue;
                if (seen.contains(n)) continue;
                Cell c = b.cellAt(n);
                if (!c.isWalkableTerrain()) continue;
                seen.add(n);
                q.addLast(n);
            }
        }
        return false;
    }
}
