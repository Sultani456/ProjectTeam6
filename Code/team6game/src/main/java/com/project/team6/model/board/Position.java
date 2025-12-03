package com.project.team6.model.board;

import java.util.*;

/**
 * Immutable integer grid coordinate.
 * Safe to use as a key in maps and sets.
 */
public final class Position {
    private final int column;
    private final int row;

    /**
     * Builds a position with column and row.
     *
     * @param column column index
     * @param row row index
     */
    public Position(int column, int row) { this.column = column; this.row = row; }

    /**
     * Returns the column value.
     *
     * @return row coordinate
     */
    public int column() { return column; }

    /**
     * Returns the row value.
     *
     * @return row coordinate
     */
    public int row() { return row; }

    /**
     * Computes Manhattan distance to another position.
     *
     * @param other target position
     * @return |x1 - x2| + |y1 - y2|
     */
    public int manhattanTo(Position other) { return Math.abs(column - other.column) + Math.abs(row - other.row); }

    /**
     * Returns a new position translated by a delta.
     *
     * @param dx change in x
     * @param dy change in y
     * @return new position at (x + dx, y + dy)
     */
    public Position translate(int dx, int dy) { return new Position(column + dx, row + dy); }

    /**
     * Returns the four direct neighbors.
     * Order is right, left, down, up.
     *
     * @return list of positions adjacent in 4 directions
     */
    public List<Position> neighbors4() {
        return List.of(
                new Position(column + 1, row),
                new Position(column - 1, row),
                new Position(column, row + 1),
                new Position(column, row - 1)
        );
    }

    /**
     * Checks structural equality by column and row.
     *
     * @param o other object
     * @return true if both positions have the same column and row
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position p)) return false;
        return column == p.column && row == p.row;
    }

    /**
     * Hash code based on row and column.
     *
     * @return hash value
     */
    @Override public int hashCode() { return Objects.hash(column, row); }

    /**
     * Returns a short string form.
     *
     * @return string like "(column,row)"
     */
    @Override public String toString() { return "(" + column + "," + row + ")"; }
}
