package com.project.team6.model.collectibles;

import com.project.team6.model.GameObject;
import com.project.team6.model.runtime.Scoreboard;

/**
 * Base class for on-cell items that affect score (positive or negative).
 */
public abstract class CollectibleObject extends GameObject {

    private final int value;           // + for rewards, - for punishments
    private final boolean requiredToWin;

    protected CollectibleObject(int x, int y, int value, boolean requiredToWin) {
        super(x, y);
        this.value = value;
        this.requiredToWin = requiredToWin;
    }

    public int value() { return value; }
    public boolean isRequiredToWin() { return requiredToWin; }

    /** Apply score effects; Board/Controller can call this when collected. */
    public void applyTo(Scoreboard scoreboard) {
        if (value >= 0) {
            if (requiredToWin) scoreboard.collectedRequired(value);
            else scoreboard.collectedOptional(value);
        } else {
            scoreboard.penalize(value); // negative OK
        }
    }
}

