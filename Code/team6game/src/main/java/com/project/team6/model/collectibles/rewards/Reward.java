package com.project.team6.model.collectibles.rewards;

// This class is for reward items.
// It is abstract. We cannot make collectibles from it directly.
// Other classes will extend this and become real rewards.

import com.project.team6.model.collectibles.CollectibleObject;

///** Abstract reward; concrete types decide symbol/value/required-flag. */
public abstract class Reward extends CollectibleObject {
    // x and y are the grid positions.
    // The child classes will decide the symbol, the score value, and if it is required.
    public Reward(int x, int y) {
        // Send the position to the GameObject parent class.
        super(x, y);
    }
}
