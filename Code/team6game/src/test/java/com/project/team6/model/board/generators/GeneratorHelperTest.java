package com.project.team6.model.board.generators;

import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.GeneratorHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests basic generator helpers. */
final class GeneratorHelperTest {

    @Test
    void perimeterWallsMakeFrame() {
        boolean[][] w = GeneratorHelper.perimeterWalls(5,7);
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 7; x++) {
                boolean border = (x==0 || x==6 || y==0 || y==4);
                assertEquals(border, w[y][x]);
            }
        }
    }

    @Test
    void toTerrainGridSetsStartAndExit() {
        int rows = 5, cols = 7;
        boolean[][] walls = new boolean[rows][cols];
        boolean[][] bars  = new boolean[rows][cols];
        Position s = new Position(0,2);
        Position e = new Position(cols-1,2);

        Cell.Terrain[][] t = GeneratorHelper.toTerrainGrid(rows, cols, walls, bars, s, e);
        assertEquals(Cell.Terrain.START, t[2][0]);
        assertEquals(Cell.Terrain.EXIT,  t[2][cols-1]);
    }
}
