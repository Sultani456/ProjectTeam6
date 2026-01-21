package com.project.team6.testutil;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.BoardGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds small boards for tests.
 */
public final class TestBoards {

    private TestBoards() {}

    /**
     * Returns a 7 by 7 board with an empty interior.
     * Start is at (0,3) and exit is at (6,3).
     */
    public static Board empty7x7() {
        int rows = 7;
        int cols = 7;

        Cell.Terrain[][] terrain = new Cell.Terrain[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                boolean border = (col == 0 || col == cols - 1 || row == 0 || row == rows - 1);
                terrain[row][col] = border ? Cell.Terrain.WALL : Cell.Terrain.FLOOR;
            }
        }

        Position start = new Position(0, 3);
        Position exit  = new Position(6, 3);
        terrain[start.row()][start.column()] = Cell.Terrain.START;
        terrain[exit.row()][exit.column()]   = Cell.Terrain.EXIT;

        BoardGenerator.Output out = new BoardGenerator.Output(rows, cols, start, exit, terrain);
        return new Board(out);
    }

    /**
     * Lists interior cells for quick placement in tests.
     */
    public static List<Position> interiorCells() {
        List<Position> list = new ArrayList<>();
        for (int row = 1; row <= 5; row++) {
            for (int col = 1; col <= 5; col++) {
                list.add(new Position(col, row));
            }
        }
        return list;
    }
}
