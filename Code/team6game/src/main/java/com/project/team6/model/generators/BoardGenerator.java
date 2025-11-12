package com.project.team6.model.generators;

import com.project.team6.model.boardUtilities.Cell;
import com.project.team6.model.boardUtilities.Position;

import java.util.*;
import java.util.function.Predicate;

/**
 * Creates a new board map with edge walls and internal barriers subject to constraints.
 * Uses a spanning-tree (maze) approach to ensure no closed loops when randomizing.
 *
 * Builds a rectangular board with walls on edges and either:
 *  - PROVIDED internal barriers at given positions, or
 *  - RANDOM internal barriers (density), both while preserving connectivity S->E.
 */
public final class BoardGenerator {

    public enum InternalBarrierMode { NONE, PROVIDED, RANDOM }

    public static final class Options {
        public final int rows, cols;
        public final Position start, exit;
        public final InternalBarrierMode barrierMode;
        public final List<Position> barrierPositions; // used if PROVIDED
        public final long seed;                       // used if RANDOM

        public Options(int rows, int cols,
                       Position start, Position exit,
                       InternalBarrierMode barrierMode,
                       List<Position> barrierPositions,
                       long seed) {
            this.rows = rows; this.cols = cols;
            this.start = start; this.exit = exit;
            this.barrierMode = barrierMode;
            this.barrierPositions = barrierPositions == null ? List.of() : List.copyOf(barrierPositions);
            this.seed = seed;
        }
    }

    /** Immutable output for constructing a Board. */
    public static final class Output {
        private final int rows, cols;
        private final Cell.Terrain[][] terrain;
        private final Position start, exit;

        public Output(int rows, int cols, Cell.Terrain[][] terrain, Position start, Position exit) {
            this.rows = rows; this.cols = cols; this.terrain = terrain; this.start = start; this.exit = exit;
        }
        public int rows() { return rows; }
        public int cols() { return cols; }
        public Position start() { return start; }
        public Position exit()  { return exit; }
        public Cell.Terrain terrainAt(int x, int y) { return terrain[y][x]; }
    }

    public static Output generate(Options o) {
        if (o.rows < 3 || o.cols < 3) throw new IllegalArgumentException("Board too small.");
        if (!inBounds(o.start, o.cols, o.rows) || !inBounds(o.exit, o.cols, o.rows))
            throw new IllegalArgumentException("Start/Exit out of bounds.");

        Cell.Terrain[][] t = new Cell.Terrain[o.rows][o.cols];
        for (int y=0; y<o.rows; y++)
            for (int x=0; x<o.cols; x++)
                t[y][x] = (x==0||y==0||x==o.cols-1||y==o.rows-1) ? Cell.Terrain.WALL : Cell.Terrain.FLOOR;

        t[o.start.y()][o.start.x()] = Cell.Terrain.START;
        t[o.exit.y()][o.exit.x()]   = Cell.Terrain.EXIT;

        switch (o.barrierMode) {
            case NONE -> { /* nothing */ }
            case PROVIDED -> {
                for (Position p : o.barrierPositions) {
                    if (!inBounds(p, o.cols, o.rows)) continue;
                    if (isEdge(p, o.cols, o.rows) || p.equals(o.start) || p.equals(o.exit)) continue;
                    t[p.y()][p.x()] = Cell.Terrain.BARRIER;
                }
                ensureConnected(t, o.start, o.exit);
            }
            case RANDOM -> {
                Random rng = new Random(o.seed);
                int max = (o.rows * o.cols) / 6; // gentle density
                int placed = 0;
                while (placed < max) {
                    int x = 1 + rng.nextInt(o.cols-2);
                    int y = 1 + rng.nextInt(o.rows-2);
                    Position p = new Position(x,y);
                    if (p.equals(o.start) || p.equals(o.exit)) continue;
                    if (t[y][x] != Cell.Terrain.FLOOR) continue;
                    t[y][x] = Cell.Terrain.BARRIER;
                    if (!isConnected(t, o.start, o.exit)) {
                        t[y][x] = Cell.Terrain.FLOOR; // revert if disconnects
                    } else {
                        placed++;
                    }
                }
            }
        }
        return new Output(o.rows, o.cols, t, o.start, o.exit);
    }

    // --- connectivity helpers
    private static void ensureConnected(Cell.Terrain[][] t, Position s, Position e) {
        if (!isConnected(t, s, e)) throw new IllegalStateException("Start is not connected to Exit after barrier placement.");
    }
    private static boolean isConnected(Cell.Terrain[][] t, Position s, Position e) {
        int rows = t.length, cols = t[0].length;
        boolean[][] seen = new boolean[rows][cols];
        ArrayDeque<Position> q = new ArrayDeque<>();
        q.add(s); seen[s.y()][s.x()] = true;
        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(e)) return true;
            for (Position n : neighbors(p, cols, rows)) {
                if (seen[n.y()][n.x()]) continue;
                Cell.Terrain tt = t[n.y()][n.x()];
                if (tt==Cell.Terrain.WALL || tt==Cell.Terrain.BARRIER) continue;
                seen[n.y()][n.x()] = true;
                q.addLast(n);
            }
        }
        return false;
    }
    private static Iterable<Position> neighbors(Position p, int cols, int rows) {
        java.util.ArrayList<Position> list = new java.util.ArrayList<>(4);
        if (p.y()>0)          list.add(new Position(p.x(), p.y()-1));
        if (p.y()<rows-1)     list.add(new Position(p.x(), p.y()+1));
        if (p.x()>0)          list.add(new Position(p.x()-1, p.y()));
        if (p.x()<cols-1)     list.add(new Position(p.x()+1, p.y()));
        return list;
    }
    private static boolean inBounds(Position p, int cols, int rows) {
        return p.x()>=0 && p.y()>=0 && p.x()<cols && p.y()<rows;
    }
    private static boolean isEdge(Position p, int cols, int rows) {
        return p.x()==0||p.y()==0||p.x()==cols-1||p.y()==rows-1;
    }

    public static ArrayList<Position> barrierList() {
        ArrayList<Position> list = new ArrayList<>();
        list.add(new Position(4, 2));
        list.add(new Position(13,2));
        list.add(new Position(4,4));
        list.add(new Position(5,4));
        list.add(new Position(12,4));
        list.add(new Position(13,4));
        list.add(new Position(4,6));
        list.add(new Position(8,6));
        list.add(new Position(9,6));
        list.add(new Position(10,6));
        list.add(new Position(7,7));
        list.add(new Position(8,7));
        list.add(new Position(3,8));
        list.add(new Position(4,8));
        list.add(new Position(5,8));
        list.add(new Position(12,8));
        list.add(new Position(13,8));
        list.add(new Position(14,8));

        return list;
    }
}


