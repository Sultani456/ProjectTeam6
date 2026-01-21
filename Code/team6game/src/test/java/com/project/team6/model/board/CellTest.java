package com.project.team6.model.board;

import com.project.team6.model.characters.Player;
import com.project.team6.model.characters.enemies.MovingEnemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests basic cell behavior.
 */
final class CellTest {

    @Test
    void walkableTerrainFlagsWork() {
        assertFalse(new Cell(Cell.Terrain.WALL).isWalkableTerrain());
        assertFalse(new Cell(Cell.Terrain.BARRIER).isWalkableTerrain());
        assertTrue(new Cell(Cell.Terrain.FLOOR).isWalkableTerrain());
        assertTrue(new Cell(Cell.Terrain.START).isWalkableTerrain());
        assertTrue(new Cell(Cell.Terrain.EXIT).isWalkableTerrain());
    }

    @Test
    void onePlayerAndOneEnemyCanShareCell() {
        Cell cell = new Cell(Cell.Terrain.FLOOR);
        Player player = new Player(new Position(1, 1));
        MovingEnemy enemy = new MovingEnemy(new Position(2, 1), 1);

        assertTrue(player.canEnter(cell));
        cell.addOccupant(player);

        assertFalse(new Player(new Position(0, 0)).canEnter(cell));

        assertTrue(enemy.canEnter(cell));
        cell.addOccupant(enemy);

        assertFalse(new MovingEnemy(new Position(0, 0), 1).canEnter(cell));
        assertTrue(cell.hasCollision());
    }

    @Test
    void enemyCannotEnterStartOrExit() {
        Cell start = new Cell(Cell.Terrain.START);
        Cell exit = new Cell(Cell.Terrain.EXIT);
        MovingEnemy enemy = new MovingEnemy(new Position(0, 0), 1);

        assertFalse(enemy.canEnter(start));
        assertFalse(enemy.canEnter(exit));
    }
}
