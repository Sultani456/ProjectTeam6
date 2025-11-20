package com.project.team6.model.collectibles.rewards;

import com.project.team6.model.board.Position;

/**
 * Required reward needed to win the game.
 * Collected by the player for points.
 */
public final class RegularReward extends Reward {

    /**
     * Creates a regular reward.
     *
     * @param position board position of the reward
     * @param value    points awarded when collected
     */
    public RegularReward(Position position, int value) {
        super(position, value, true);
    }

    /**
     * Returns the ASCII symbol for a regular reward.
     *
     * @return '.'
     */
    @Override public char symbol() { return '.'; }
}
