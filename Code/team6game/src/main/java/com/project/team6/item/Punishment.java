package com.project.team6.item;


/** Punishment (negative score) — your '*' tiles. */
public class Punishment extends Item {
    private final int penalty;

    public Punishment(int x, int y, int penalty) {
        super(x, y);
        this.penalty = penalty;
    }

    @Override
    public char symbol() { return '*'; }

    @Override
    public int value() { return -Math.abs(penalty); } // always negative
}
