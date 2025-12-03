package com.project.team6.model.board.generators; 

import com.project.team6.controller.GameConfig;
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

    /** Policy for choosing start and exit positions. */
    private final StartExitSelector startExitSelector;

    /**
     * Selects start and exit positions for a given board size.
     * This is intentionally small so policies can be swapped in tests or future modes.
     */
    public interface StartExitSelector {
        StartExit select(int rows, int cols);
    }

    private static final class StartExit {
        private final Position start;
        private final Position exit;

        private StartExit(Position start, Position exit) {
            this.start = start;
            this.exit = exit;
        }
    }

    private static final class RandomEdgeStartExitSelector implements StartExitSelector {
        private final Random rng;

        private RandomEdgeStartExitSelector(Random rng) {
            this.rng = rng;
        }

        @Override
        public StartExit select(int rows, int cols) {
            Position start = GeneratorHelper.randomEdgeStart(rows, cols, rng);
            Position exit  = GeneratorHelper.randomEdgeExit(rows, cols, rng);
            return new StartExit(start, exit);
        }
    }

    /**
     * Default constructor for production use.
     * Uses a new Random instance and the default random edge policy.
     */
    public BoardGenerator() {
        this(new Random());
    }

    /**
     * Constructor that accepts a Random.
     * Uses the default random edge policy with that Random.
     *
     * @param rng random source
     */
    public BoardGenerator(Random rng) {
        this(rng, new RandomEdgeStartExitSelector(Objects.requireNonNull(rng)));
    }

    /**
     * Constructor that accepts both a Random and a start/exit policy.
     * This keeps the generator deterministic and also decouples the policy.
     *
     * @param rng random source used for random barriers
     * @param startExitSelector policy for choosing start and exit
     */
    public BoardGenerator(Random rng, StartExitSelector startExitSelector) {
        this.rng = Objects.requireNonNull(rng);
        this.startExitSelector = Objects.requireNonNull(startExitSelector);
    }

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


    /**
     * Generates a terrain layout based on options.
     *
     * @param opts options for barriers and size
     * @return generated output
     * @throws NullPointerException if opts is null
     */
    public Output generate(BarrierOptions opts) {
        Objects.requireNonNull(opts);

        return switch (opts.barrierMode) {
            case NONE      -> generateNone(opts);
            case PROVIDED  -> generateProvided(opts);
            case TEXT      -> generateFromText(opts);
            case RANDOM    -> generateRandomWithConstraints(opts);
        };
    }

    private StartExit chooseStartExit(int rows, int cols) {
        return startExitSelector.select(rows, cols);
    }

    // --------------------------------------------------------------------
    // NONE
    // --------------------------------------------------------------------

    private Output generateNone(BarrierOptions opts) {
        GeneratorHelper.validateSize(opts.rows, opts.cols);
        boolean[][] walls = GeneratorHelper.perimeterWalls(opts.rows, opts.cols);
        boolean[][] barriers = new boolean[opts.rows][opts.cols];

        StartExit startExit = chooseStartExit(opts.rows, opts.cols);

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(
                        opts.rows, opts.cols, walls, barriers, startExit.start, startExit.exit
                );
        return new Output(opts.rows, opts.cols, startExit.start, startExit.exit, terrain);
    }

    // --------------------------------------------------------------------
    // PROVIDED
    // --------------------------------------------------------------------

    private Output generateProvided(BarrierOptions opts) {
        GeneratorHelper.validateSize(opts.rows, opts.cols);
        boolean[][] walls    = GeneratorHelper.perimeterWalls(opts.rows, opts.cols);
        boolean[][] barriers = new boolean[opts.rows][opts.cols];

        if (opts.barrierPositions != null) {
            for (Position p : opts.barrierPositions) {
                if (p.column() <= 0 || p.column() >= opts.cols - 1 ||
                        p.row() <= 0 || p.row() >= opts.rows - 1) {
                    continue;
                }
                barriers[p.row()][p.column()] = true;
            }
        }

        StartExit startExit = chooseStartExit(opts.rows, opts.cols);

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(
                        opts.rows, opts.cols, walls, barriers, startExit.start, startExit.exit
                );
        return new Output(opts.rows, opts.cols, startExit.start, startExit.exit, terrain);
    }

    // --------------------------------------------------------------------
    // TEXT
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
            StartExit startExit = chooseStartExit(rows, cols);
            start = startExit.start;
            exit  = startExit.exit;
        }

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
    }

    // --------------------------------------------------------------------
    // RANDOM
    // --------------------------------------------------------------------

    private Output generateRandomWithConstraints(BarrierOptions opts) {
        int rows = opts.rows;
        int cols = opts.cols;
        GeneratorHelper.validateSize(rows, cols);

        boolean[][] walls    = GeneratorHelper.perimeterWalls(rows, cols);
        boolean[][] barriers = new boolean[rows][cols];

        StartExit startExit = chooseStartExit(rows, cols);
        Position start = startExit.start;
        Position exit  = startExit.exit;

        int interior = (rows - 2) * (cols - 2);
        int targetBarriers = Math.max(0,
                (int) Math.round(interior * GameConfig.boardBarrierPercentage));

        int placed = 0;
        int attempts = 0;
        int maxAttempts = targetBarriers * 20 + 100;

        while (placed < targetBarriers && attempts < maxAttempts) {
            attempts++;

            int column = 1 + rng.nextInt(cols - 2);
            int row = 1 + rng.nextInt(rows - 2);

            if (walls[row][column] || barriers[row][column]) continue;

            Position p = new Position(column, row);

            if (Board.chebyshev(p, start) < 2 || Board.chebyshev(p, exit) < 2) {
                continue;
            }

            barriers[row][column] = true;

            if (!GeneratorHelper.isBarrierConfigurationValid(walls, barriers, start, exit)) {
                barriers[row][column] = false;
            } else {
                placed++;
            }
        }

        Cell.Terrain[][] terrain =
                GeneratorHelper.toTerrainGrid(rows, cols, walls, barriers, start, exit);
        return new Output(rows, cols, start, exit, terrain);
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
