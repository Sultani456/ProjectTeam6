package com.project.team6.model.board.generators.barrierProperties;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Position;

import java.util.List;
import java.util.Objects;

/**
 * Options for barrier generation.
 * For TEXT mode, rows and cols are ignored because the file defines size.
 * This object is immutable.
 */
public final class BarrierOptions {

    /** Number of board rows. */
    public final int rows = GameConfig.rows;

    /** Number of board columns. */
    public final int cols =  GameConfig.cols;

    /** Mode that controls how barriers are created. */
    public final BarrierMode barrierMode;

    /** Positions of internal barriers. Used only in PROVIDED mode. */
    public final List<Position> barrierPositions = GameConfig.barrierList;

    /** Classpath resource for the map. Used only in TEXT mode. */
    public final String mapResource = GameConfig.mapResource;

    /**
     * Builds a set of options for generation.
     *
     * @param barrierMode    barrier generation mode
     * @throws NullPointerException if {@code barrierMode} is null
     */
    public BarrierOptions(BarrierMode barrierMode) {
        this.barrierMode = Objects.requireNonNull(barrierMode);
    }
}
