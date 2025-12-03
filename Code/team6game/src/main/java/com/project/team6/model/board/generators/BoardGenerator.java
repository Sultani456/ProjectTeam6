package com.project.team6.model.board.generators; 

import com.project.team6.model.board.*;
import com.project.team6.model.board.generators.barrierProperties.BarrierOptions;
import com.project.team6.model.board.generators.helpers.GeneratorHelper;

import java.util.*;

/**
 * Builds the first terrain for a board.
 * It does not place rewards, punishments, or enemies. Spawner handles that.
 */
public final class BoardGenerator {

    /** Random source for start/exit and random barriers. */
    private final Random rng;

    /**
     * Default constructor for production use.
     * Uses a new Random instance.
     */
    public BoardGenerator() {
        this(new Random());
    }

    /**
     * Constructor that accepts a Random.
     * Useful for deterministic tests.
     *
     * @param rng random source
     */
    public BoardGenerator(Random rng) {
        this.rng = Objects.requireNonNull(rng);
    }

    // --------------------------------------------------------------------
    // Output type
    // --------------------------------------------------------------------

    /**
     * Result of terrain generation.
     * Holds size, start and exit, and the terrain grid.
     */
    public static final class Output {
        private final int rows;
        private final int cols;
        private final Position start;
        private final Position exit;
        private final Cell.Terrain[][] terrain;

        /**
         * Creates an output record.
         *
         * @param rows     number of rows
         * @param cols     number of columns
         * @param start    start position
         * @param exit     exit position
         * @param terrain  terrain grid
         */
        public Output(int rows, int cols,
                      Position start, Position exit,
                      Cell.Terrain[][] terrain) {
            this.rows = rows;
            this.cols = cols;
            this.start = start;
            this.exit = exit;
            this.terrain = terrain;
        }

        /** @return number of rows */
        public int rows() {return rows;}
        /** @return number of columns */
        public int cols() {return cols;}
        /** @return start position */
        public Position start() {return start;}
        /** @return exit position */
        public Position exit() {return exit;}
        /** @return terrain grid */
        public Cell.Terrain[][] terrain() {return terrain;}

    }

    // --------------------------------------------------------------------
    // Public API
    // --------------------------------------------------------------------

    /**
     * Generates a terrain layout based on options.
     *
     * @param opts options for barriers and size
     * @param boardBarrierPercentage target fraction of interior barriers for RANDOM mode
     * @return generated output
     * @throws NullPointerException if opts is null
     */
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
    // NONE: just perimeter walls, empty interior
    // --------------------------------------------------------------------

    /**
     * Makes a board with only perimeter walls.
     * Interior has no barriers.
     */
    private Output generateNone(BarrierOptions opts) {
        GeneratorHelper.validateSize(opts.rows, opts.cols);
        boolean[][] walls = GeneratorHelper.perimeterWalls(opts.rows, opts.cols);
        boolean[][] barriers = new boolean[opts.rows][opts.cols];

        Position start = GeneratorHelper.randomEdgeStart(opts.rows, opts.cols, rng);
        Position exit  = GeneratorHelper.randomEdgeExit(opts.rows, opts.cols, rng);

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(opts.rows, opts.cols, walls, barriers, start, exit);
        return new Output(opts.rows, opts.cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // PROVIDED: perimeter and programmer provided barrier list
    // --------------------------------------------------------------------

    /**
     * Makes a board with perimeter walls and a provided barrier list.
     * Ignores any barrier placed on the perimeter.
     */
    private Output generateProvided(BarrierOptions opts) {
        GeneratorHelper.validateSize(opts.rows, opts.cols);
        boolean[][] walls    = GeneratorHelper.perimeterWalls(opts.rows, opts.cols);
        boolean[][] barriers = new boolean[opts.rows][opts.cols];

        if (opts.barrierPositions != null) {
            for (Position p : opts.barrierPositions) {
                if (p.column() <= 0 || p.column() >= opts.cols - 1 ||
                        p.row() <= 0 || p.row() >= opts.rows - 1) {
                    // ignore perimeter; those are already walls
                    continue;
                }
                barriers[p.row()][p.column()] = true;
            }
        }

        Position start = GeneratorHelper.randomEdgeStart(opts.rows, opts.cols, rng);
        Position exit  = GeneratorHelper.randomEdgeExit(opts.rows, opts.cols, rng);

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(opts.rows, opts.cols, walls, barriers, start, exit);
        return new Output(opts.rows, opts.cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // TEXT: read terrain from a level file on the classpath
    // --------------------------------------------------------------------

    /**
     * Loads terrain from a text map.
     * Uses characters to set walls, barriers, start, and exit.
     *
     * @throws NullPointerException if mapResource is null
     * @throws IllegalArgumentException if the map is empty or lines have different length
     */
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

        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            for (int column = 0; column < cols; column++) {
                char ch = line.charAt(column);
                switch (ch) {
                    case 'X' -> walls[row][column] = true;
                    case '#' -> barriers[row][column] = true;
                    case 'S' -> start = new Position(column, row);
                    case 'E' -> exit  = new Position(column, row);
                    default  -> { /* floor */ }
                }
            }
        }

        if (start == null || exit == null) {
            // fall back to random edges if not provided in file
            start = GeneratorHelper.randomEdgeStart(rows, cols, rng);
            exit  = GeneratorHelper.randomEdgeExit(rows, cols, rng);
        }

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // RANDOM: perimeter and random internal barriers with constraints
    // --------------------------------------------------------------------

    /**
     * Makes random interior barriers while keeping the board playable.
     * Keeps distance from start and exit and checks connectivity.
     *
     * @param opts options for size
     * @param boardBarrierPercentage target interior fraction for barriers
     * @return generated output
     */
    private Output generateRandomWithConstraints(BarrierOptions opts,
                                                 double boardBarrierPercentage) {
        int rows = opts.rows;
        int cols = opts.cols;
        GeneratorHelper.validateSize(rows, cols);

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

            int column = 1 + rng.nextInt(cols - 2);
            int row = 1 + rng.nextInt(rows - 2);

            if (walls[row][column] || barriers[row][column]) continue;

            Position p = new Position(column, row);

            // keep at least Chebyshev 2 away from start and exit
            if (Board.chebyshev(p, start) < 2 || Board.chebyshev(p, exit) < 2) {
                continue;
            }

            // tentatively place barrier
            barriers[row][column] = true;

            if (!GeneratorHelper.isBarrierConfigurationValid(walls, barriers, start, exit)) {
                barriers[row][column] = false; // revert
            } else {
                placed++;
            }
        }

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
    }

    /**
     * Returns a fixed list of barrier positions for the PROVIDED mode.
     *
     * @return list of positions for barriers
     */
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

    /**
     * Factory for tests that need deterministic layouts.
     *
     * @param seed random seed
     * @return BoardGenerator with fixed Random
     */
    public static BoardGenerator withSeed(long seed) {
        return new BoardGenerator(new Random(seed));
    }
}
