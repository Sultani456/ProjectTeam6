package com.project.team6.model.boardUtilities;


import com.project.team6.model.characters.CharacterObject;
import com.project.team6.model.characters.enemies.Enemy;
import com.project.team6.model.characters.Player;
import com.project.team6.model.collectibles.CollectibleObject;

import java.util.Objects;

public final class Cell {
    public enum Terrain { FLOOR, WALL, BARRIER, START, EXIT }

    private final Terrain terrain;                 // immutable terrain
    private CharacterObject occupant;              // null = none
    private CollectibleObject item;                // null = none

    public Cell(Terrain terrain) { this.terrain = Objects.requireNonNull(terrain); }

    public Terrain terrain()                 { return terrain; }
    public CharacterObject occupant()        { return occupant; }
    public void setOccupant(CharacterObject occupant) { this.occupant = occupant; }

    public CollectibleObject item()          { return item; }
    public void setItem(CollectibleObject item){ this.item = item; }

    public boolean isWalkableTerrain() {
        return terrain != Terrain.WALL && terrain != Terrain.BARRIER;
    }
    public boolean isEnterableNow() {
        return isWalkableTerrain() && occupant == null;
    }

    /** ASCII symbol for quick display; GUI can ignore this. */
    public char symbol() {
        if (terrain == Terrain.WALL)    return 'X';
        if (terrain == Terrain.BARRIER) return '#';
        if (terrain == Terrain.START)   return 'S';
        if (terrain == Terrain.EXIT)    return 'E';
        if (occupant != null) {
            if (occupant instanceof Player) return 'P';
            if (occupant instanceof Enemy)  return 'B';
        }
        if (item != null) return item.symbol();  // CollectibleObject already defines symbol()
        return ' ';
    }
}

