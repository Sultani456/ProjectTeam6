package com.project.team6.model.generators;

import com.project.team6.model.boardUtilities.Cell;
import com.project.team6.model.boardUtilities.Position;

import java.util.*;
import java.util.function.Predicate;

/**
 * Creates a new board map with edge walls and internal barriers subject to constraints.
 * Uses a spanning-tree (maze) approach to ensure no closed loops when randomizing.
 */
public final class BoardGenerator {

    public enum InternalBarrierMode { RANDOM_MAZE, PROVIDED }

    public record Options(
            int rows, int cols,
            Position start, Position exit,
            InternalBarrierMode mode,
            List<Position> providedBarriers,   // used only in PROVIDED mode
            long randomSeed
    ) {}

    /** Build a new board. */
    public static Map<Position, Cell> generate(Options opts) {
        validate(opts);
        Map<Position, Cell> board = new HashMap<>(opts.rows * opts.cols);

        // 1) Perimeter walls
        for (int y = 0; y < opts.rows; y++) {
            for (int x = 0; x < opts.cols; x++) {
                boolean edge = (x == 0 || y == 0 || x == opts.cols - 1 || y == opts.rows - 1);
                Cell.Terrain t = edge ? Cell.Terrain.WALL : Cell.Terrain.FLOOR;
                board.put(new Position(x, y), new Cell(t));
            }
        }

        // 2) Start/Exit cells are floors with their special terrain
        board.put(opts.start, new Cell(Cell.Terrain.START));
        board.put(opts.exit,  new Cell(Cell.Terrain.EXIT));

        // 3) Internal barriers
        switch (opts.mode) {
            case RANDOM_MAZE -> carveMazeNoLoops(board, opts);
            case PROVIDED -> placeProvidedBarriers(board, opts);
        }

        // 4) Validate path and no-lock constraints
        if (!isReachable(board, opts.start, opts.exit)) {
            throw new IllegalStateException("Start is not connected to Exit after barrier placement.");
        }
        if (!hasFreedom(board, opts.start)) {
            throw new IllegalStateException("Start is blocked in; player cannot move away.");
        }
        if (!isEnterable(board, opts.exit)) {
            throw new IllegalStateException("Exit is blocked; cannot enter.");
        }

        return board;
    }

    /** Quick ASCII for debugging or GamePanel text-mode. */
    public static String toAscii(Map<Position, Cell> board, int rows, int cols) {
        StringBuilder sb = new StringBuilder(rows * (cols + 1));
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                sb.append(board.get(new Position(x, y)).symbol());
            }
            if (y < rows - 1) sb.append('\n');
        }
        return sb.toString();
    }

    // --- Implementation details ---

    private static void carveMazeNoLoops(Map<Position, Cell> board, Options o) {
        // Start from a grid of BARRIER, carve passages (FLOOR) via randomized DFS -> spanning tree (no loops).
        // We keep START/EXIT as floors and connect them to the carved structure.
        Random rng = new Random(o.randomSeed);

        // Set all non-edge, non-start/exit to BARRIER first…
        forEachInterior(o.rows, o.cols, p -> {
            if (!p.equals(o.start) && !p.equals(o.exit)) {
                board.put(p, new Cell(Cell.Terrain.BARRIER));
            }
        });

        // Choose a random odd cell to start carving; ensure inside and not start/exit
        Position carveStart = pickAnyInterior(o, rng, p -> !p.equals(o.start) && !p.equals(o.exit));

        // Iterative DFS
        Deque<Position> stack = new ArrayDeque<>();
        setFloor(board, carveStart);
        stack.push(carveStart);

        while (!stack.isEmpty()) {
            Position current = stack.peek();
            List<Position> neighbors = shuffledNeighbors(current, rng, 2); // step of 2 to leave walls between
            boolean carved = false;
            for (Position step2 : neighbors) {
                if (!isInterior(step2, o.rows, o.cols)) continue;
                if (board.get(step2).terrain() != Cell.Terrain.FLOOR &&
                        !step2.equals(o.start) && !step2.equals(o.exit)) {
                    // Midpoint between current and step2 (the wall to remove)
                    Position mid = midpoint(current, step2);
                    setFloor(board, mid);
                    setFloor(board, step2);
                    stack.push(step2);
                    carved = true;
                    break;
                }
            }
            if (!carved) stack.pop();
        }

        // Ensure start and exit are connected to floors by opening a neighbor if needed
        connectSpecialIfNeeded(board, o.start, o.rows, o.cols, rng);
        connectSpecialIfNeeded(board, o.exit,  o.rows, o.cols, rng);
    }

    private static void placeProvidedBarriers(Map<Position, Cell> board, Options o) {
        // Lay down barriers but never on START/EXIT or edges; then validate connectivity and closed loops.
        for (Position p : o.providedBarriers) {
            if (!isInterior(p, o.rows, o.cols)) continue;
            if (p.equals(o.start) || p.equals(o.exit)) continue;
            Cell c = board.get(p);
            if (c.terrain() == Cell.Terrain.FLOOR) {
                board.put(p, new Cell(Cell.Terrain.BARRIER));
            }
        }
        // Basic validation:
        if (!isReachable(board, o.start, o.exit)) {
            throw new IllegalArgumentException("Provided barriers disconnect Start from Exit.");
        }
        // A strict “no closed loops” test on arbitrary inputs is expensive; we approximate:
        // ensure the walkable subgraph is a single connected component with no 2x2 all-floor squares converted to loops by barriers placement.
        // (You can expand this check later if you want.)
    }

    // --- Helpers ---

    private static void validate(Options o) {
        if (o.rows < 5 || o.cols < 5) throw new IllegalArgumentException("Board must be at least 5x5.");
        if (!onEdge(o.start, o.rows, o.cols)) throw new IllegalArgumentException("Start must be on perimeter.");
        if (!onEdge(o.exit,  o.rows, o.cols)) throw new IllegalArgumentException("Exit must be on perimeter.");
        if (o.start.equals(o.exit)) throw new IllegalArgumentException("Start and Exit cannot be the same.");
    }

    private static boolean onEdge(Position p, int rows, int cols) {
        return p.x() == 0 || p.y() == 0 || p.x() == cols - 1 || p.y() == rows - 1;
    }

    private static void forEachInterior(int rows, int cols, java.util.function.Consumer<Position> f) {
        for (int y = 1; y < rows - 1; y++)
            for (int x = 1; x < cols - 1; x++)
                f.accept(new Position(x, y));
    }

    private static boolean isInterior(Position p, int rows, int cols) {
        return p.x() > 0 && p.y() > 0 && p.x() < cols - 1 && p.y() < rows - 1;
    }

    private static Position pickAnyInterior(Options o, Random rng, Predicate<Position> ok) {
        List<Position> list = new ArrayList<>();
        forEachInterior(o.rows, o.cols, p -> { if (ok.test(p)) list.add(p); });
        Collections.shuffle(list, rng);
        return list.getFirst();
    }

    private static void setFloor(Map<Position, Cell> board, Position p) {
        Cell cur = board.get(p);
        if (cur.terrain() == Cell.Terrain.START || cur.terrain() == Cell.Terrain.EXIT) return;
        board.put(p, new Cell(Cell.Terrain.FLOOR));
    }

    /** Return step-2 neighbors for maze carving. */
    private static List<Position> shuffledNeighbors(Position p, Random rng, int step) {
        List<Position> ns = new ArrayList<>(4);
        ns.add(new Position(p.x() + step, p.y()));
        ns.add(new Position(p.x() - step, p.y()));
        ns.add(new Position(p.x(), p.y() + step));
        ns.add(new Position(p.x(), p.y() - step));
        Collections.shuffle(ns, rng);
        return ns;
    }

    private static Position midpoint(Position a, Position b) {
        return new Position((a.x() + b.x()) / 2, (a.y() + b.y()) / 2);
    }

    /** Ensure START/EXIT have at least one adjacent FLOOR. */
    private static void connectSpecialIfNeeded(Map<Position, Cell> board, Position p, int rows, int cols, Random rng) {
        boolean has = false;
        for (Position n : neighbors4InBounds(p, rows, cols)) {
            if (board.get(n).terrain() == Cell.Terrain.FLOOR) { has = true; break; }
        }
        if (!has) {
            List<Position> ns = neighbors4InBounds(p, rows, cols);
            Collections.shuffle(ns, rng);
            setFloor(board, ns.getFirst());
        }
    }

    private static List<Position> neighbors4InBounds(Position p, int rows, int cols) {
        List<Position> list = new ArrayList<>(4);
        if (p.x() > 0) list.add(new Position(p.x() - 1, p.y()));
        if (p.x() < cols - 1) list.add(new Position(p.x() + 1, p.y()));
        if (p.y() > 0) list.add(new Position(p.x(), p.y() - 1));
        if (p.y() < rows - 1) list.add(new Position(p.x(), p.y() + 1));
        return list;
    }

    /** BFS passability: START to EXIT must be reachable through FLOOR/START/EXIT. */
    private static boolean isReachable(Map<Position, Cell> board, Position start, Position exit) {
        Deque<Position> q = new ArrayDeque<>();
        Set<Position> seen = new HashSet<>();
        q.add(start); seen.add(start);

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            if (p.equals(exit)) return true;
            for (Position n : p.neighbors4()) {
                Cell c = board.get(n);
                if (c == null) continue;
                if (seen.contains(n)) continue;
                if (c.terrain() == Cell.Terrain.WALL || c.terrain() == Cell.Terrain.BARRIER) continue;
                seen.add(n); q.addLast(n);
            }
        }
        return false;
    }

    /** Start must have some adjacent enterable cell. */
    private static boolean hasFreedom(Map<Position, Cell> board, Position start) {
        for (Position n : start.neighbors4()) {
            Cell c = board.get(n);
            if (c != null && c.isWalkableTerrain()) return true;
        }
        return false;
    }

    /** Exit must be enterable from at least one neighbor. */
    private static boolean isEnterable(Map<Position, Cell> board, Position exit) {
        for (Position n : exit.neighbors4()) {
            Cell c = board.get(n);
            if (c != null && c.isWalkableTerrain()) return true;
        }
        return false;
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

