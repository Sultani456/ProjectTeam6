package com.project.team6.model.board.generators;

import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.GeneratorHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Small tests for generator helper methods.
 */
final class GeneratorHelperTest {

    @Test
    void perimeterWallsMakeFrame() {
        boolean[][] walls = GeneratorHelper.perimeterWalls(5, 7);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                boolean border = (col == 0 || col == 6 || row == 0 || row == 4);
                assertEquals(border, walls[row][col]);
            }
        }
    }

    @Test
    void toTerrainGridSetsStartAndExit() {
        int rows = 5;
        int cols = 7;
        boolean[][] walls = new boolean[rows][cols];
        boolean[][] bars  = new boolean[rows][cols];
        Position start = new Position(0, 2);
        Position exit  = new Position(cols - 1, 2);

        Cell.Terrain[][] terrain = GeneratorHelper.toTerrainGrid(rows, cols, walls, bars, start, exit);
        assertEquals(Cell.Terrain.START, terrain[2][0]);
        assertEquals(Cell.Terrain.EXIT,  terrain[2][cols - 1]);
    }
}
