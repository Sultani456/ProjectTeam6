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

    /** Change in x for this direction. */
    public final int dx;

    /** Change in y for this direction. */
    public final int dy;

    /**
     * Creates a direction with x and y deltas.
     *
     * @param dx change in x
     * @param dy change in y
     */
    Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
}
