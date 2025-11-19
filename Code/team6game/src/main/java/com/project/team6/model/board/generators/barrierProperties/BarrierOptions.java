package com.project.team6.model.board.generators.barrierProperties;

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
    public final int rows;

    /** Number of board columns. */
    public final int cols;

    /** Mode that controls how barriers are created. */
    public final BarrierMode barrierMode;

    /** Positions of internal barriers. Used only in PROVIDED mode. */
    public final List<Position> barrierPositions;

    /** Classpath resource for the map. Used only in TEXT mode. */
    public final String mapResource;

    /**
     * Builds a set of options for generation.
     *
     * @param rows           number of rows
     * @param cols           number of columns
     * @param barrierMode    barrier generation mode
     * @param barrierPositions positions to place barriers when using PROVIDED
     * @param mapResource    classpath resource for the map when using TEXT
     * @throws NullPointerException if {@code barrierMode} is null
     */
    public BarrierOptions(int rows, int cols,
                          BarrierMode barrierMode,
                          List<Position> barrierPositions,
                          String mapResource) {
        this.rows = rows;
        this.cols = cols;
        this.barrierMode = Objects.requireNonNull(barrierMode);
        this.barrierPositions = (barrierPositions == null ? List.of() : List.copyOf(barrierPositions));
        this.mapResource = mapResource;
    }
}
