package com.project.team6.model.board;

import java.util.*;

/**
 * Immutable integer grid coordinate.
 * Safe to use as a key in maps and sets.
 */
public final class Position {
    private final int x;
    private final int y;

    /**
     * Builds a position with x and y.
     *
     * @param x column index
     * @param y row index
     */
    public Position(int x, int y) { this.x = x; this.y = y; }

    /**
     * Returns the x value.
     *
     * @return x coordinate
     */
    public int x() { return x; }

    /**
     * Returns the y value.
     *
     * @return y coordinate
     */
    public int y() { return y; }

    /**
     * Computes Manhattan distance to another position.
     *
     * @param other target position
     * @return |x1 - x2| + |y1 - y2|
     */
    public int manhattanTo(Position other) { return Math.abs(x - other.x) + Math.abs(y - other.y); }

    /**
     * Returns a new position translated by a delta.
     *
     * @param dx change in x
     * @param dy change in y
     * @return new position at (x + dx, y + dy)
     */
    public Position translate(int dx, int dy) { return new Position(x + dx, y + dy); }

    /**
     * Returns the four direct neighbors.
     * Order is right, left, down, up.
     *
     * @return list of positions adjacent in 4 directions
     */
    public List<Position> neighbors4() {
        return List.of(
                new Position(x + 1, y),
                new Position(x - 1, y),
                new Position(x, y + 1),
                new Position(x, y - 1)
        );
    }

    /**
     * Checks structural equality by x and y.
     *
     * @param o other object
     * @return true if both positions have the same x and y
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position p)) return false;
        return x == p.x && y == p.y;
    }

    /**
     * Hash code based on x and y.
     *
     * @return hash value
     */
    @Override public int hashCode() { return Objects.hash(x, y); }

    /**
     * Returns a short string form.
     *
     * @return string like "(x,y)"
     */
    @Override public String toString() { return "(" + x + "," + y + ")"; }
}
