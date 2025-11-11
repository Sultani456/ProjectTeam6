package com.project.team6.model.collectibles.rewards;

// This class is for reward items.
// It is abstract. We cannot make collectibles from it directly.
// Other classes will extend this and become real rewards.

import com.project.team6.model.collectibles.CollectibleObject;

/** Positive-score base. */
public abstract class Reward extends CollectibleObject {
    protected Reward(int x, int y, int value, boolean requiredToWin) {
        super(x, y, value, requiredToWin);
    }
}

