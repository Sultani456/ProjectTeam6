package com.project.team6.model.collectibles;

import com.project.team6.model.GameObject;
import com.project.team6.model.boardUtilities.Position;
import com.project.team6.model.runtime.Scoreboard;

/**
 * Base class for on-cell items that affect score (positive or negative).
 */
public abstract class CollectibleObject extends GameObject {

    private final int value;           // + for rewards, - for punishments
    private final boolean requiredToWin;

    protected CollectibleObject(Position position, int value, boolean requiredToWin) {
        super(position);
        this.value = value;
        this.requiredToWin = requiredToWin;
    }

    public int value() { return value; }
    public boolean isRequiredToWin() { return requiredToWin; }

}

