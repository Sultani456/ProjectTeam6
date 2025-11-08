package com.project.team6.model.boardUtilities;

import java.util.Objects;

/**
 * Immutable integer (x,y) grid position.
 * Records (x,y), equality, and hashCode for use in sets/maps.
 */
public final class Position {
    // x coordinate on the grid
    private final int x;
    // y coordinate on the grid
    private final int y;

    // Make a position with given x and y. Values never change after this.
    public Position(int x, int y) { this.x = x; this.y = y; }

    // Get x value
    public int x() { return x; }
    // Get y value
    public int y() { return y; }

    // Return a new position moved by dx and dy. Does not change the current one.
    public Position withDxDy(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    // Two positions are equal if both x and y match.
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position p)) return false;
        return x == p.x && y == p.y;
    }

    // Hash for using this in a HashMap or HashSet.
    @Override public int hashCode() {
        return Objects.hash(x, y);
    }

    // Show the position like "(x,y)" when we print it.
    @Override public String toString() {
        return "(" + x + "," + y + ")";
    }
}
