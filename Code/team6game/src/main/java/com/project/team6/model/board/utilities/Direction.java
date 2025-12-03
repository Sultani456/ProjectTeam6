package com.project.team6.model.board.utilities;

/**
 * Four way grid directions with integer deltas.
 * Used to move one cell at a time.
 */
public enum Direction {
    /** Move up by one row. */
    UP(0, -1),

    /** Move down by one row. */
    DOWN(0, 1),

    /** Move left by one column. */
    LEFT(-1, 0),

    /** Move right by one column. */
    RIGHT(1, 0);

    /** Change in column for this direction. */
    public final int d_column;

    /** Change in row for this direction. */
    public final int d_row;

    /**
     * Creates a direction with x and y deltas.
     *
     * @param d_column change in column
     * @param d_row change in row
     */
    Direction(int d_column, int d_row) { this.d_column = d_column; this.d_row = d_row; }
}
