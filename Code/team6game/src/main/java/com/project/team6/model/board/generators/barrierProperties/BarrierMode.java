package com.project.team6.model.board.generators.barrierProperties;

/**
 * Selects how barriers are created on the board.
 * All modes include a solid perimeter wall.
 */
public enum BarrierMode {

    /**
     * No internal barriers.
     * The interior is empty space.
     */
    NONE,

    /**
     * Use internal barriers provided in code.
     * Start and exit are randomized on the west and east edges.
     */
    PROVIDED,

    /**
     * Read the terrain from a text file on the classpath.
     * Example: "maps/level1.txt".
     */
    TEXT,

    /**
     * Create internal barriers randomly.
     * The layout changes each run.
     */
    RANDOM
}
