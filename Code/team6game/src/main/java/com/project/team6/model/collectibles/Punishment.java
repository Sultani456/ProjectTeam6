package com.project.team6.model.collectibles;

import com.project.team6.controller.GameConfig;
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
     */
    public Punishment(Position position) {
        super(position, Math.min(0, GameConfig.punishmentPenalty), false); // ensure non-positive
    }

    /**
     * Returns the ASCII symbol for this item.
     *
     * @return '*'
     */
    @Override public char symbol() { return '*'; }
}
