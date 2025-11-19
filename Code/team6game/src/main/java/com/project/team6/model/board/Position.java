package com.project.team6.model.board;

import java.util.*;

/**
 * Immutable integer grid coordinate.
 * Safe as a HashMap key.
 */
public final class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) { this.x = x; this.y = y; }

    public int x() { return x; }
    public int y() { return y; }

    /** Manhattan distance. */
    public int manhattanTo(Position other) { return Math.abs(x - other.x) + Math.abs(y - other.y); }

    /** Return a new position moved by (dx, dy). */
    public Position translate(int dx, int dy) { return new Position(x + dx, y + dy); }

    /** 4-neighborhood (up, down, left, right). */
    public List<Position> neighbors4() {
        return List.of(
                new Position(x + 1, y),
                new Position(x - 1, y),
                new Position(x, y + 1),
                new Position(x, y - 1)
        );
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position p)) return false;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
    @Override public String toString() { return "(" + x + "," + y + ")"; }
}

