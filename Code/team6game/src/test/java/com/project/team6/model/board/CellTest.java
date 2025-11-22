package com.project.team6.model.board;

import com.project.team6.model.characters.Player;
import com.project.team6.model.characters.enemies.MovingEnemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests basic cell rules. */
final class CellTest {

    @Test
    void walkableTerrain_flagsWork() {
        assertFalse(new Cell(Cell.Terrain.WALL).isWalkableTerrain());
        assertFalse(new Cell(Cell.Terrain.BARRIER).isWalkableTerrain());
        assertTrue(new Cell(Cell.Terrain.FLOOR).isWalkableTerrain());
        assertTrue(new Cell(Cell.Terrain.START).isWalkableTerrain());
        assertTrue(new Cell(Cell.Terrain.EXIT).isWalkableTerrain());
    }

    @Test
    void enterable_onePlayer_oneEnemy() {
        Cell c = new Cell(Cell.Terrain.FLOOR);
        Player p = new Player(new Position(1, 1));
        MovingEnemy e = new MovingEnemy(new Position(2, 1), 1);

        assertTrue(c.isEnterableFor(p));
        c.addOccupant(p);
        assertFalse(c.isEnterableFor(new Player(new Position(0,0))));
        assertTrue(c.isEnterableFor(e));
        c.addOccupant(e);
        assertFalse(c.isEnterableFor(new MovingEnemy(new Position(0,0), 1)));
        assertTrue(c.hasCollision());
    }

    @Test
    void enemyCannotEnterStartOrExit() {
        Cell start = new Cell(Cell.Terrain.START);
        Cell exit  = new Cell(Cell.Terrain.EXIT);
        MovingEnemy e = new MovingEnemy(new Position(0,0), 1);

        assertFalse(start.isEnterableFor(e));
        assertFalse(exit.isEnterableFor(e));
    }
}
