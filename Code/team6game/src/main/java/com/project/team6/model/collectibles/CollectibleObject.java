package com.project.team6.model.collectibles;

import com.project.team6.model.GameObject;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;

/**
 * Base class for items placed on a cell.
 * Items change the score when collected.
 * Rewards add points. Punishments remove points.
 */
public abstract class CollectibleObject extends GameObject {

    /** Score value for this item. Positive for rewards. Negative for punishments. */
    private final int value;

    /** True if this item is required to win. */
    private final boolean requiredToWin;

    /**
     * Creates a collectible item.
     *
     * @param position      board position of the item
     * @param value         score change when collected
     * @param requiredToWin true if the item is required to win
     */
    protected CollectibleObject(Position position, int value, boolean requiredToWin) {
        super(position);
        this.value = value;
        this.requiredToWin = requiredToWin;
    }

    /**
     * Returns the score value of this item.
     *
     * @return positive for rewards or negative for punishments
     */
    public int value() { return value; }

    /**
     * Reports if this item is required to win.
     *
     * @return true if required to win
     */
    public boolean isRequiredToWin() { return requiredToWin; }

}
