package com.project.team6.model.collectibles;

import com.project.team6.model.board.Position;

/**
 * Item that reduces the score.
 * Not required to win.
 */
public final class Punishment extends CollectibleObject {

    /**
     * Creates a punishment item.
     * The penalty is clamped to zero or below.
     *
     * @param position tile where the item is placed
     * @param penalty  negative points applied on collection
     */
    public Punishment(Position position, int penalty) {
        super(position, Math.min(0, penalty), false); // ensure non-positive
    }

    /**
     * Returns the ASCII symbol for this item.
     *
     * @return '*'
     */
    @Override public char symbol() { return '*'; }
}
