package com.project.team6.util;

import java.util.Objects;

/**
 * Immutable integer (x,y) grid position.
 * Records (x,y), equality, and hashCode for use in sets/maps.
 */
public final class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) { this.x = x; this.y = y; }

    public int x() { return x; }
    public int y() { return y; }

    public Position withDxDy(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position p)) return false;
        return x == p.x && y == p.y;
    }

    @Override public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override public String toString() {
        return "(" + x + "," + y + ")";
    }
}
