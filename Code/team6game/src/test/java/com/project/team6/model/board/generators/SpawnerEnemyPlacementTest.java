package com.project.team6.model.board.generators;

import com.project.team6.controller.GameController;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.testutil.TestBoards;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies enemy spawning does not block the gate tiles
 * and does not cut off the path from Start to Exit.
 */
final class SpawnerEnemyPlacementTest {

    @Test
    void doesNotUseGateFrontTiles() {
        Board b = TestBoards.empty7x7();
        Spawner sp = new Spawner(b, GameController.DEFAULT_TICK_MS);

        sp.spawnEnemies(3, 2); // place a few enemies

        Position s = b.start();
        Position e = b.exit();
        Position startFront = new Position(s.column() + 1, s.row());
        Position exitFront  = new Position(e.column() - 1, e.row());

        assertFalse(b.cellAt(startFront).hasEnemy(), "Enemy at start front tile");
        assertFalse(b.cellAt(exitFront).hasEnemy(),  "Enemy at exit front tile");
    }

    @Test
    void keepsStartToExitReachable() {
        Board b = TestBoards.empty7x7();
        Spawner sp = new Spawner(b, GameController.DEFAULT_TICK_MS);

        sp.spawnEnemies(5, 2); // allow several placements

        // Collect all enemy positions as blocked for path check
        Set<Position> blocked = new HashSet<>();
        for (int y = 0; y < b.rows(); y++) {
            for (int x = 0; x < b.cols(); x++) {
                Cell c = b.cellAt(new Position(x, y));
                if (c.hasEnemy()) {
                    blocked.add(new Position(x, y));
                }
            }
        }

        // Path from Start to Exit must still exist
        assertTrue(
            SpawnerHelper.canReach(b, b.start(), b.exit(), blocked),
            "Enemies should not block the Start to Exit path"
        );
    }
}
