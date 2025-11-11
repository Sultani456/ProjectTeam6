package com.project.team6.model.boardUtilities;

public final class Cell {
    public enum Terrain { FLOOR, WALL, BARRIER, START, EXIT }

    private final Terrain terrain;                 // immutable terrain
    private CharacterObject occupant;              // null = none
    private CollectibleObject item;                // null = none

    public Cell(Terrain terrain) { this.terrain = Objects.requireNonNull(terrain); }

    public Terrain terrain()                 { return terrain; }
    public CharacterObject occupant()        { return occupant; }
    public void setOccupant(CharacterObject o) { this.occupant = o; }

    public CollectibleObject item()          { return item; }
    public void setItem(CollectibleObject it){ this.item = it; }

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
            // Prefer polymorphism if you add a display method; until then:
            if (occupant instanceof Player) return 'P';
            if (occupant instanceof Enemy)  return 'B';
        }
        if (item != null) return item.symbol();  // your CollectibleObject already defines symbol()
        return ' ';
    }
}

