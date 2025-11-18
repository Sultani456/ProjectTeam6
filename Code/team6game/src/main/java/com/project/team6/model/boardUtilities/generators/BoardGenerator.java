package com.project.team6.model.boardUtilities.generators;

import com.project.team6.model.boardUtilities.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Builds immutable terrain + start/exit for a Board.
 *
 * Legend for TEXT maps:
 *   'X' -> WALL, '#' -> BARRIER, everything else -> FLOOR
 */
public final class BoardGenerator {
    /** Result returned to Board. */
    public static final class Output {
        private final int rows, cols;
        private final Position start, exit;
        private final Cell.Terrain[][] terrain;

        public Output(int rows, int cols, Position start, Position exit, Cell.Terrain[][] terrain) {
            this.rows = rows;
            this.cols = cols;
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

    public Output generate(BarrierOptions opts, double boardBarrierPercentage) {
        Objects.requireNonNull(opts);
        Random rng = new Random();

        return switch (opts.barrierMode) {
            case NONE -> generateEmpty(opts);
            case PROVIDED -> generateProvided(opts);
            case TEXT -> generateFromText(opts);
            case RANDOM -> generateRandomWithConstraints(opts, boardBarrierPercentage);
        };
    }

    private Output generateEmpty(BarrierOptions opts) {
        int rows = opts.rows;
        int cols = opts.cols;
        validateSize(rows, cols);

        boolean[][] walls    = perimeterWalls(rows, cols);
        boolean[][] barriers = new boolean[rows][cols];

        Random rng = new Random();
        Position start = randomEdgeStart(rows, cols, rng);
        Position exit  = randomEdgeExit(rows, cols, rng);

        Cell.Terrain[][] grid = toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, grid);
    }

    private Output generateProvided(BarrierOptions opts) {
        int rows = opts.rows;
        int cols = opts.cols;
        validateSize(rows, cols);

        boolean[][] walls    = perimeterWalls(rows, cols);
        boolean[][] barriers = new boolean[rows][cols];

        for (Position p : opts.barrierPositions) {
            if (!isInterior(p.x(), p.y(), rows, cols)) continue;
            barriers[p.y()][p.x()] = true;
        }

        Random rng = new Random();
        Position start = randomEdgeStart(rows, cols, rng);
        Position exit  = randomEdgeExit(rows, cols, rng);
        return new Output(rows, cols, start, exit, toTerrainGrid(rows, cols, walls, barriers, start, exit));
    }

    private Output generateFromText(BarrierOptions opts) {
        TextMap tm = parseTextMapFromResource(opts.mapResource);

        Random rng = new Random();
        Position start = randomEdgeStart(tm.rows, tm.cols, rng);
        Position exit  = randomEdgeExit(tm.rows, tm.cols, rng);
        return new Output(tm.rows, tm.cols, start, exit, tm.terrain);
    }

    private Output generateRandomWithConstraints(BarrierOptions opts, double boardBarrierPercentage) {
        int rows = opts.rows;
        int cols = opts.cols;

        validateSize(rows, cols);
        Random rng = new Random();

        boolean[][] walls    = perimeterWalls(rows, cols);
        boolean[][] barriers = new boolean[rows][cols];

        // Target barrier count (~10% interior) – tweak as desired
        int interior = (rows - 2) * (cols - 2);
        int targetBarriers = Math.max(0, (int) Math.round(interior * boardBarrierPercentage));

        Position start = randomEdgeStart(rows, cols, rng);
        Position exit  = randomEdgeExit(rows, cols, rng);

        int placed = 0;
        int attempts = 0;
        int maxAttempts = targetBarriers * 20 + 100; // avoid infinite loops

        while (placed < targetBarriers && attempts < maxAttempts) {
            attempts++;

            int x = 1 + rng.nextInt(cols - 2);
            int y = 1 + rng.nextInt(rows - 2);

            // already a barrier or wall?
            if (walls[y][x] || barriers[y][x]) {
                continue;
            }

            Position p = new Position(x, y);

            // Distance constraint: keep at least Chebyshev 2 away from Start/Exit
            if (Board.chebyshev(p, start) < 2 || Board.chebyshev(p, exit) < 2) {
                continue;
            }

            // Tentatively place barrier
            barriers[y][x] = true;

            if (!isBarrierConfigurationValid(walls, barriers, start, exit)) {
                // invalid -> revert
                barriers[y][x] = false;
            } else {
                placed++;
            }
        }

        Cell.Terrain[][] terrain = toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
    }


    // ---------------------------------------------------------------------
    // Helpers

    private static void validateSize(int rows, int cols) {
        if (rows < 3 || cols < 3) {
            throw new IllegalArgumentException("rows/cols must be >= 3");
        }
    }

    private static boolean isInterior(int x, int y, int rows, int cols) {
        return x > 0 && x < cols - 1 && y > 0 && y < rows - 1;
    }

    /**
     * Checks whether the current (walls + barriers) configuration is valid under your rules:
     *  - Start and Exit reachable from Start.
     *  - All floor cells are reachable (single connected region; no unreachable pockets).
     *  - No "1x1 prison cells" (floor cells with 0 passable 4-neighbors).
     */
    private boolean isBarrierConfigurationValid(boolean[][] walls,
                                                boolean[][] barriers,
                                                Position start,
                                                Position exit) {
        int rows = walls.length;
        int cols = walls[0].length;

        // Build terrain with START/EXIT for convenience
        Cell.Terrain[][] terrain = toTerrainGrid(rows, cols, walls, barriers, start, exit);

        // BFS from start over passable tiles
        boolean[][] visited = new boolean[rows][cols];
        int reachableFloors = BFSCount(terrain, start, visited);

        if (!visited[exit.y()][exit.x()]) {
            return false; // Exit not reachable
        }

        int totalFloors = countFloors(terrain);
        if (reachableFloors < totalFloors) {
            return false; // Some floor cells unreachable => multiple regions / enclosed pockets
        }

        // Local "no 1x1 pocket" rule: no floor cell with 0 passable neighbors
        if (hasIsolatedFloor(terrain)) {
            return false;
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // BFS + reachability helpers

    private static boolean isPassable(Cell.Terrain t) {
        return switch (t) {
            case WALL, BARRIER -> false;
            default -> true; // FLOOR, START, EXIT treated as passable
        };
    }

    /**
     * BFS from start over passable tiles. Returns how many floor-like cells were reached
     * and fills visited[y][x].
     */
    private static int BFSCount(Cell.Terrain[][] terrain, Position start, boolean[][] visited) {
        int rows = terrain.length;
        int cols = terrain[0].length;

        Deque<Position> q = new ArrayDeque<>();
        q.add(start);
        visited[start.y()][start.x()] = true;
        int count = 0;

        while (!q.isEmpty()) {
            Position p = q.removeFirst();
            count++;

            int x = p.x();
            int y = p.y();

            // 4-neighbors
            if (y > 0) {
                tryVisit(terrain, visited, q, x, y - 1);
            }
            if (y < rows - 1) {
                tryVisit(terrain, visited, q, x, y + 1);
            }
            if (x > 0) {
                tryVisit(terrain, visited, q, x - 1, y);
            }
            if (x < cols - 1) {
                tryVisit(terrain, visited, q, x + 1, y);
            }
        }
        return count;
    }

    private static void tryVisit(Cell.Terrain[][] terrain,
                                 boolean[][] visited,
                                 Deque<Position> q,
                                 int x, int y) {
        if (!visited[y][x] && isPassable(terrain[y][x])) {
            visited[y][x] = true;
            q.addLast(new Position(x, y));
        }
    }

    /**
     * Counts how many cells are passable (non-wall, non-barrier).
     */
    private static int countFloors(Cell.Terrain[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        int count = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (isPassable(terrain[y][x])) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns true if there exists a floor cell with 0 passable 4-neighbors.
     * This avoids tiny isolated "1x1 pockets".
     */
    private static boolean hasIsolatedFloor(Cell.Terrain[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (!isPassable(terrain[y][x])) continue;

                int open = 0;
                if (y > 0 && isPassable(terrain[y - 1][x])) open++;
                if (y < rows - 1 && isPassable(terrain[y + 1][x])) open++;
                if (x > 0 && isPassable(terrain[y][x - 1])) open++;
                if (x < cols - 1 && isPassable(terrain[y][x + 1])) open++;

                if (open == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // ––––––
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

    private static Cell.Terrain[][] toTerrainGrid(int rows,
                                                  int cols,
                                                  boolean[][] walls,
                                                  boolean[][] barriers,
                                                  Position start,
                                                  Position exit) {

        Cell.Terrain[][] t = new Cell.Terrain[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                // Start overrides everything else
                if (start != null && x == start.x() && y == start.y()) {
                    t[y][x] = Cell.Terrain.START;
                    continue;
                }

                // Exit overrides everything else
                if (exit != null && x == exit.x() && y == exit.y()) {
                    t[y][x] = Cell.Terrain.EXIT;
                    continue;
                }

                // Otherwise: WALL > BARRIER > FLOOR
                if (walls[y][x]) {
                    t[y][x] = Cell.Terrain.WALL;
                } else if (barriers[y][x]) {
                    t[y][x] = Cell.Terrain.BARRIER;
                } else {
                    t[y][x] = Cell.Terrain.FLOOR;
                }
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



