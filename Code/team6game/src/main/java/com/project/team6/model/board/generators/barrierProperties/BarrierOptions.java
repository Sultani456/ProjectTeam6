package com.project.team6.model.board.generators.barrierProperties;

import com.project.team6.model.board.Position;

import java.util.List;
import java.util.Objects;

/** Inputs for generation.  For TEXT, rows/cols are ignored (taken from file). */
public final class BarrierOptions {
    public final int rows;
    public final int cols;
    public final BarrierMode barrierMode;
    public final List<Position> barrierPositions; // used only in PROVIDED
    public final String mapResource;              // used only in TEXT, e.g., "maps/level1.txt"


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
