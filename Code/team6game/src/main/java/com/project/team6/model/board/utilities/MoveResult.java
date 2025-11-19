package com.project.team6.model.board.utilities;

/**
 * Result of a move attempt.
 * Used by the board and controller.
 */
public enum MoveResult {
    /** Move succeeded and no collision happened. */
    MOVED,

    /** Move was not allowed due to bounds, terrain, or occupancy. */
    BLOCKED,

    /** Move entered a cell with an enemy. */
    COLLISION
}
