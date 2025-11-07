package com.project.team6.item; // items live in this package

/** Punishment (negative score) — your '*' tiles. */
public class Punishment extends Item {
    private final int penalty; // how many points this takes away

    public Punishment(int x, int y, int penalty) {
        super(x, y); // set the tile position
        this.penalty = penalty; // store the penalty amount
    }

    @Override
    public char symbol() { return '*'; } // this shows as '*' on the map

    @Override
    public int value() { 
        // make sure the value is negative so score goes down
        return -Math.abs(penalty); // always negative
    }
}
