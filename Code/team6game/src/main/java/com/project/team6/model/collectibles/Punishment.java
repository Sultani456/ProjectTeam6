package com.project.team6.model.collectibles;

import com.project.team6.model.boardUtilities.Position;

/** Negative-score item. */
public final class Punishment extends CollectibleObject {

    public Punishment(Position position, int penalty) {
        super(position, Math.min(0, penalty), false); // ensure non-positive
    }

    @Override public char symbol() { return '*'; }
}
