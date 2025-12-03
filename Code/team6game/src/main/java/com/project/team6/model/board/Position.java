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
     * Returns a short string form.
     *
     * @return string like "(column,row)"
     */
    @Override public String toString() { return "(" + column + "," + row + ")"; }
}
