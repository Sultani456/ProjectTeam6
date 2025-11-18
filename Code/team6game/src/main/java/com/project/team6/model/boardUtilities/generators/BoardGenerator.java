package com.project.team6.model.boardUtilities.generators;

import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.boardUtilities.generators.barrierProperties.BarrierOptions;
import com.project.team6.model.boardUtilities.generators.helpers.GeneratorHelper;

import java.util.*;

/**
 * Builds the initial terrain for a Board.
 * It does NOT place rewards, punishments, or enemies – that is Spawner's job.
 */
public final class BoardGenerator {

    // --------------------------------------------------------------------
    // Output type
    // --------------------------------------------------------------------

    public static final class Output {
        private final int rows;
        private final int cols;
        private final Position start;
        private final Position exit;
        private final Cell.Terrain[][] terrain;

        public Output(int rows, int cols,
                      Position start, Position exit,
                      Cell.Terrain[][] terrain) {
            this.rows = rows;
            this.cols = cols;
            this.start = start;
            this.exit = exit;
            this.terrain = terrain;
        }

        public int rows() {return rows;}
        public int cols() {return cols;}
        public Position start() {return start;}
        public Position exit() {return exit;}
        public Cell.Terrain[][] terrain() {return terrain;}

    }

    // --------------------------------------------------------------------
    // Public API
    // --------------------------------------------------------------------

    public Output generate(BarrierOptions opts, double boardBarrierPercentage) {
        Objects.requireNonNull(opts);

        return switch (opts.barrierMode) {
            case NONE      -> generateNone(opts);
            case PROVIDED  -> generateProvided(opts);
            case TEXT      -> generateFromText(opts);
            case RANDOM    -> generateRandomWithConstraints(opts, boardBarrierPercentage);
        };
    }

    // --------------------------------------------------------------------
    // NONE – just perimeter walls, empty interior
    // --------------------------------------------------------------------

    private Output generateNone(BarrierOptions opts) {
        GeneratorHelper.validateSize(opts.rows, opts.cols);
        boolean[][] walls = GeneratorHelper.perimeterWalls(opts.rows, opts.cols);
        boolean[][] barriers = new boolean[opts.rows][opts.cols];

        Random rng = new Random();
        Position start = GeneratorHelper.randomEdgeStart(opts.rows, opts.cols, rng);
        Position exit  = GeneratorHelper.randomEdgeExit(opts.rows, opts.cols, rng);

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(opts.rows, opts.cols, walls, barriers, start, exit);
        return new Output(opts.rows, opts.cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // PROVIDED – perimeter + programmer-provided barrier list
    // --------------------------------------------------------------------

    private Output generateProvided(BarrierOptions opts) {
        GeneratorHelper.validateSize(opts.rows, opts.cols);
        boolean[][] walls    = GeneratorHelper.perimeterWalls(opts.rows, opts.cols);
        boolean[][] barriers = new boolean[opts.rows][opts.cols];

        if (opts.barrierPositions != null) {
            for (Position p : opts.barrierPositions) {
                if (p.x() <= 0 || p.x() >= opts.cols - 1 ||
                        p.y() <= 0 || p.y() >= opts.rows - 1) {
                    // ignore perimeter; those are already walls
                    continue;
                }
                barriers[p.y()][p.x()] = true;
            }
        }

        Random rng = new Random();
        Position start = GeneratorHelper.randomEdgeStart(opts.rows, opts.cols, rng);
        Position exit  = GeneratorHelper.randomEdgeExit(opts.rows, opts.cols, rng);

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(opts.rows, opts.cols, walls, barriers, start, exit);
        return new Output(opts.rows, opts.cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // TEXT – read terrain from a level*.txt on classpath
    // --------------------------------------------------------------------

    private Output generateFromText(BarrierOptions opts) {
        Objects.requireNonNull(opts.mapResource,
                "TEXT mode requires a mapResource (e.g., \"maps/level1.txt\")");

        List<String> lines = GeneratorHelper.readLinesFromResource(opts.mapResource);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Empty map resource: " + opts.mapResource);
        }

        int rows = lines.size();
        int cols = lines.get(0).length();
        for (String line : lines) {
            if (line.length() != cols) {
                throw new IllegalArgumentException("Inconsistent line length in map file.");
            }
        }

        boolean[][] walls    = new boolean[rows][cols];
        boolean[][] barriers = new boolean[rows][cols];
        Position start = null;
        Position exit  = null;

        for (int y = 0; y < rows; y++) {
            String line = lines.get(y);
            for (int x = 0; x < cols; x++) {
                char ch = line.charAt(x);
                switch (ch) {
                    case 'X' -> walls[y][x] = true;
                    case '#' -> barriers[y][x] = true;
                    case 'S' -> start = new Position(x, y);
                    case 'E' -> exit  = new Position(x, y);
                    default  -> { /* floor */ }
                }
            }
        }

        if (start == null || exit == null) {
            // fall back to random edges if not provided in file
            Random rng = new Random();
            start = GeneratorHelper.randomEdgeStart(rows, cols, rng);
            exit  = GeneratorHelper.randomEdgeExit(rows, cols, rng);
        }

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // RANDOM – perimeter + random internal barriers with constraints
    // --------------------------------------------------------------------

    private Output generateRandomWithConstraints(BarrierOptions opts,
                                                 double boardBarrierPercentage) {
        int rows = opts.rows;
        int cols = opts.cols;
        GeneratorHelper.validateSize(rows, cols);

        Random rng = new Random();

        boolean[][] walls    = GeneratorHelper.perimeterWalls(rows, cols);
        boolean[][] barriers = new boolean[rows][cols];

        Position start = GeneratorHelper.randomEdgeStart(rows, cols, rng);
        Position exit  = GeneratorHelper.randomEdgeExit(rows, cols, rng);

        int interior = (rows - 2) * (cols - 2);
        int targetBarriers = Math.max(0,
                (int) Math.round(interior * boardBarrierPercentage));

        int placed = 0;
        int attempts = 0;
        int maxAttempts = targetBarriers * 20 + 100;

        while (placed < targetBarriers && attempts < maxAttempts) {
            attempts++;

            int x = 1 + rng.nextInt(cols - 2);
            int y = 1 + rng.nextInt(rows - 2);

            if (walls[y][x] || barriers[y][x]) continue;

            Position p = new Position(x, y);

            // keep at least Chebyshev 2 away from start/exit
            if (Board.chebyshev(p, start) < 2 || Board.chebyshev(p, exit) < 2) {
                continue;
            }

            // tentatively place barrier
            barriers[y][x] = true;

            if (!GeneratorHelper.isBarrierConfigurationValid(walls, barriers, start, exit)) {
                barriers[y][x] = false; // revert
            } else {
                placed++;
            }
        }

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
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




