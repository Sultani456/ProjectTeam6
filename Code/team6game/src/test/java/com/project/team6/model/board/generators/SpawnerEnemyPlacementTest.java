package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
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
 * Enemy spawning on the fixed 7 by 7 test board.
 */
final class SpawnerEnemyPlacementTest {

    /**
     * Enemies must not stand right in front of the gates.
     */
    @Test
    void doesNotUseGateFrontTiles() {
        Board board = TestBoards.empty7x7();

        GameConfig.numEnemies = 3;
        GameConfig.enemyMovePeriod = 2;

        Spawner spawner = Spawner.withSeed(board, 10L);
        spawner.spawnEnemies();

        Position start = board.start();
        Position exit  = board.exit();
        Position startFront = new Position(start.column() + 1, start.row());
        Position exitFront  = new Position(exit.column() - 1, exit.row());

        assertFalse(board.cellAt(startFront).hasEnemy(), "Enemy at start front tile");
        assertFalse(board.cellAt(exitFront).hasEnemy(),  "Enemy at exit front tile");
    }

    /**
     * Path from start to exit is still open after enemies are placed.
     */
    @Test
    void keepsStartToExitReachable() {
        Board board = TestBoards.empty7x7();

        GameConfig.numEnemies = 5;
        GameConfig.enemyMovePeriod = 2;

        Spawner spawner = Spawner.withSeed(board, 11L);
        spawner.spawnEnemies();

        Set<Position> blocked = new HashSet<>();
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position p = new Position(col, row);
                Cell c = board.cellAt(p);
                if (c.hasEnemy()) {
                    blocked.add(p);
                }
            }
        }

        assertTrue(
                SpawnerHelper.canReach(board, board.start(), board.exit(), blocked),
                "Enemies should not block the Start to Exit path"
        );
    }
}
