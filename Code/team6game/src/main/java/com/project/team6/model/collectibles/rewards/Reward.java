package com.project.team6.model.collectibles.rewards;

// This class is for reward items.
// It is abstract. We cannot make collectibles from it directly.
// Other classes will extend this and become real rewards.

import com.project.team6.model.board.Position;
import com.project.team6.model.collectibles.CollectibleObject;

/**
 * Base class for reward items.
 * Rewards add positive score when collected.
 * Subclasses provide specific types of rewards.
 */
public abstract class Reward extends CollectibleObject {

    /**
     * Creates a reward.
     *
     * @param position       board position of the reward
     * @param value          points granted on collection
     * @param requiredToWin  true if this reward is required to win
     */
    protected Reward(Position position, int value, boolean requiredToWin) {
        super(position, value, requiredToWin);
    }


}
