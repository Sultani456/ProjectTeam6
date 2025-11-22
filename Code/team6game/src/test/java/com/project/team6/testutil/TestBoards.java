package com.project.team6.testutil;

import com.project.team6.model.board.*;
import com.project.team6.model.board.generators.BoardGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds small boards for tests.
 * Uses a 7x7 board with walls on the border.
 * Start is at (0,3). Exit is at (6,3).
 */
public final class TestBoards {

    private TestBoards() {}

    /** Returns a 7x7 board with an empty interior. */
    public static Board empty7x7() {
        int rows = 7, cols = 7;

        Cell.Terrain[][] t = new Cell.Terrain[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                boolean border = (x == 0 || x == cols - 1 || y == 0 || y == rows - 1);
                t[y][x] = border ? Cell.Terrain.WALL : Cell.Terrain.FLOOR;
            }
        }
        Position start = new Position(0, 3);
        Position exit  = new Position(6, 3);
        t[start.y()][start.x()] = Cell.Terrain.START;
        t[exit.y()][exit.x()]   = Cell.Terrain.EXIT;

        BoardGenerator.Output out = new BoardGenerator.Output(rows, cols, start, exit, t);
        return new Board(out);
    }

    /** Lists interior cells for quick placement in tests. */
    public static List<Position> interiorCells() {
        List<Position> list = new ArrayList<>();
        for (int y = 1; y <= 5; y++) {
            for (int x = 1; x <= 5; x++) {
                list.add(new Position(x, y));
            }
        }
        return list;
    }
}
