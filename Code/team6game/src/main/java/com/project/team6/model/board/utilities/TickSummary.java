package com.project.team6.model.board.utilities;

/**
 * Summary of events for a single tick.
 * Stores whether the player was caught.
 */
public final class TickSummary {
    /** True when an enemy reached the player this tick. */
    private final boolean playerCaught;

    /**
     * Builds a summary for one tick.
     *
     * @param playerCaught true if the player was caught
     */
    public TickSummary(boolean playerCaught) {
        this.playerCaught = playerCaught;
    }

    /**
     * Reports if the player was caught this tick.
     *
     * @return true if caught, false otherwise
     */
    public boolean playerCaught() {
        return playerCaught;
    }
}
