package com.project.team6.model.generators;

import com.project.team6.model.boardUtilities.Cell;
import com.project.team6.model.boardUtilities.Position;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Builds immutable terrain + start/exit for a Board.
 * Modes:
 *  - NONE: perimeter walls only, empty interior
 *  - PROVIDED: perimeter walls + programmer-provided internal barriers; start/exit randomized on west/east edges
 *  - RANDOM: perimeter walls + random internal barriers
 *  - TEXT: read terrain from a text file on the classpath (e.g., "maps/level1.txt")
 *
 * Legend for TEXT maps:
 *   'X' -> WALL, '#' -> BARRIER, everything else -> FLOOR
 */
public final class BoardGenerator {

    public enum InternalBarrierMode { NONE, PROVIDED, RANDOM, TEXT }

    /** Inputs for generation.  For TEXT, rows/cols are ignored (taken from file). */
    public static final class Options {
        public final int rows;
        public final int cols;
        public final InternalBarrierMode barrierMode;
        public final List<Position> barrierPositions; // used only in PROVIDED
        public final String mapResource;              // used only in TEXT, e.g., "maps/level1.txt"


        public Options(int rows, int cols,
                       InternalBarrierMode barrierMode,
                       List<Position> barrierPositions,
                       String mapResource) {
            this.rows = rows;
            this.cols = cols;
            this.barrierMode = Objects.requireNonNull(barrierMode);
            this.barrierPositions = (barrierPositions == null ? List.of() : List.copyOf(barrierPositions));
            this.mapResource = mapResource;
        }
    }

    /** Result returned to Board. */
    public static final class Output {
        private final int rows, cols;
        private final Position start, exit;
        private final Cell.Terrain[][] terrain;

        public Output(int rows, int cols, Position start, Position exit, Cell.Terrain[][] terrain) {
            this.rows = rows; this.cols = cols;
            this.start = Objects.requireNonNull(start);
            this.exit  = Objects.requireNonNull(exit);
            this.terrain = Objects.requireNonNull(terrain);
        }

        public int rows() { return rows; }
        public int cols() { return cols; }
        public Position start() { return start; }
        public Position exit() { return exit; }
        public Cell.Terrain terrainAt(int x, int y) { return terrain[y][x]; }
        public Cell.Terrain[][] terrain() { return terrain; }
    }

    // ---------------------------------------------------------------------

    public Output generate(Options opts) {
        Objects.requireNonNull(opts);
        Random rng = new Random();

        switch (opts.barrierMode) {
            case TEXT: {
                TextMap tm = parseTextMapFromResource(opts.mapResource);
                Position start = randomEdgeStart(tm.rows, tm.cols, rng);
                Position exit  = randomEdgeExit(tm.rows, tm.cols, rng);
                return new Output(tm.rows, tm.cols, start, exit, tm.terrain);
            }

            case PROVIDED: {
                if (opts.rows < 3 || opts.cols < 3) {
                    throw new IllegalArgumentException("rows/cols must be >= 3.");
                }
                boolean[][] walls    = perimeterWalls(opts.rows, opts.cols);
                boolean[][] barriers = new boolean[opts.rows][opts.cols];

                for (Position p : opts.barrierPositions) {
                    if (p.x() <= 0 || p.x() >= opts.cols - 1 || p.y() <= 0 || p.y() >= opts.rows - 1) continue;
                    barriers[p.y()][p.x()] = true;
                }
                Position start = randomEdgeStart(opts.rows, opts.cols, rng);
                Position exit  = randomEdgeExit(opts.rows, opts.cols, rng);
                return new Output(opts.rows, opts.cols, start, exit, toTerrainGrid(opts.rows, opts.cols, walls, barriers));
            }

            case RANDOM: {
                if (opts.rows < 3 || opts.cols < 3) {
                    throw new IllegalArgumentException("rows/cols must be >= 3.");
                }
                boolean[][] walls    = perimeterWalls(opts.rows, opts.cols);
                boolean[][] barriers = new boolean[opts.rows][opts.cols];

                // simple random internal barrier density (~10% of interior)
                int interior = (opts.rows - 2) * (opts.cols - 2);
                int count = Math.max(0, (int)Math.round(interior * 0.10));
                for (int i = 0; i < count; i++) {
                    int x = 1 + rng.nextInt(opts.cols - 2);
                    int y = 1 + rng.nextInt(opts.rows - 2);
                    barriers[y][x] = true;
                }

                Position start = randomEdgeStart(opts.rows, opts.cols, rng);
                Position exit  = randomEdgeExit(opts.rows, opts.cols, rng);
                return new Output(opts.rows, opts.cols, start, exit, toTerrainGrid(opts.rows, opts.cols, walls, barriers));
            }

            case NONE: {
                if (opts.rows < 3 || opts.cols < 3) {
                    throw new IllegalArgumentException("rows/cols must be >= 3.");
                }
                boolean[][] walls    = perimeterWalls(opts.rows, opts.cols);
                boolean[][] barriers = new boolean[opts.rows][opts.cols];
                Position start = randomEdgeStart(opts.rows, opts.cols, rng);
                Position exit  = randomEdgeExit(opts.rows, opts.cols, rng);
                return new Output(opts.rows, opts.cols, start, exit, toTerrainGrid(opts.rows, opts.cols, walls, barriers));
            }
        }
        throw new IllegalArgumentException("Unsupported mode: " + opts.barrierMode);
    }

    // ---------------------------------------------------------------------
    // Helpers

    private static boolean[][] perimeterWalls(int rows, int cols) {
        boolean[][] walls = new boolean[rows][cols];
        for (int y = 0; y < rows; y++) {
            walls[y][0] = true; walls[y][cols - 1] = true;
        }
        for (int x = 0; x < cols; x++) {
            walls[0][x] = true; walls[rows - 1][x] = true;
        }
        return walls;
    }

    private static Cell.Terrain[][] toTerrainGrid(int rows, int cols,
                                                  boolean[][] walls, boolean[][] barriers) {
        Cell.Terrain[][] t = new Cell.Terrain[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (walls[y][x])       t[y][x] = Cell.Terrain.WALL;
                else if (barriers[y][x]) t[y][x] = Cell.Terrain.BARRIER;
                else                   t[y][x] = Cell.Terrain.FLOOR;
            }
        }
        return t;
    }

    private static Position randomEdgeStart(int rows, int cols, Random rng) {
        int y = 1 + rng.nextInt(rows - 2); // west edge, avoid corners
        return new Position(0, y);
    }

    private static Position randomEdgeExit(int rows, int cols, Random rng) {
        int y = 1 + rng.nextInt(rows - 2); // east edge, avoid corners
        return new Position(cols - 1, y);
    }

    // ---- TEXT parsing ----

    private static final class TextMap {
        final int rows, cols;
        final Cell.Terrain[][] terrain;
        TextMap(int rows, int cols, Cell.Terrain[][] terrain) {
            this.rows = rows; this.cols = cols; this.terrain = terrain;
        }
    }

    /**
     * Reads a classpath resource (e.g., "maps/level1.txt") and builds a terrain grid.
     * Legend: 'X' = WALL, '#' = BARRIER, else FLOOR.
     */
    private TextMap parseTextMapFromResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("TEXT mode requires mapResource (e.g., \"maps/level1.txt\").");
        }

        List<String> lines = new ArrayList<>();
        try (var in = BoardGenerator.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String s;
                while ((s = br.readLine()) != null) {
                    if (!s.isEmpty()) lines.add(s);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read map resource: " + resourcePath, e);
        }

        if (lines.isEmpty()) throw new IllegalArgumentException("Empty map file: " + resourcePath);
        int rows = lines.size();
        int cols = lines.get(0).length();
        for (String s : lines) {
            if (s.length() != cols) throw new IllegalArgumentException("Non-rectangular map: line width mismatch.");
        }

        Cell.Terrain[][] terrain = new Cell.Terrain[rows][cols];
        for (int y = 0; y < rows; y++) {
            String s = lines.get(y);
            for (int x = 0; x < cols; x++) {
                char ch = s.charAt(x);
                switch (ch) {
                    case 'X': terrain[y][x] = Cell.Terrain.WALL;    break;
                    case '#': terrain[y][x] = Cell.Terrain.BARRIER; break;
                    default : terrain[y][x] = Cell.Terrain.FLOOR;   break;
                }
            }
        }
        return new TextMap(rows, cols, terrain);
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



