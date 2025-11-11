package com.project.team6.model.collectibles;

/** Negative-score item. */
public final class Punishment extends CollectibleObject {

    public Punishment(int x, int y, int penalty) {
        super(x, y, Math.min(0, penalty), false); // ensure non-positive
    }

    @Override public char symbol() { return '*'; }
}
