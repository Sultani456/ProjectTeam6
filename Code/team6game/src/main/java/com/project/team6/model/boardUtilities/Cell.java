package com.project.team6.model.boardUtilities;


import com.project.team6.model.characters.CharacterObject;
import com.project.team6.model.characters.enemies.Enemy;
import com.project.team6.model.characters.Player;
import com.project.team6.model.collectibles.CollectibleObject;

import java.util.Objects;

/** One tile on the board: at most one Player and at most one Enemy. */
public final class Cell {

    public enum Terrain { FLOOR, WALL, BARRIER, START, EXIT }

    private final Terrain terrain;
    private CollectibleObject item; // optional
    private Player playerOcc;       // null if none
    private Enemy  enemyOcc;        // null if none

    public Cell(Terrain terrain) { this.terrain = Objects.requireNonNull(terrain); }

    // --- Accessors
    public Terrain terrain()                 { return terrain; }
    public CollectibleObject item()          { return item; }
    public void setItem(CollectibleObject i) { this.item = i; }

    public Player player()      { return playerOcc; }
    public Enemy  enemy()       { return enemyOcc; }
    public boolean hasPlayer()  { return playerOcc != null; }
    public boolean hasEnemy()   { return enemyOcc  != null; }
    public boolean hasCollision(){ return hasPlayer() && hasEnemy(); }

    // --- Terrain
    public boolean isWalkableTerrain() {
        return terrain != Terrain.WALL && terrain != Terrain.BARRIER;
    }

    /** Only one Player and one Enemy allowed. Other side being present is fine. */
    public boolean isEnterableFor(CharacterObject who) {
        if (!isWalkableTerrain() || who == null) return false;
        if (who instanceof Player) return playerOcc == null;
        if (who instanceof Enemy)  return enemyOcc  == null;
        return false;
    }

    // --- Occupancy management
    public void addOccupant(CharacterObject obj) {
        Objects.requireNonNull(obj);
        if (obj instanceof Player p) {
            if (playerOcc != null && playerOcc != p) throw new IllegalStateException("Cell already has a Player.");
            playerOcc = p;
        } else if (obj instanceof Enemy e) {
            if (enemyOcc != null && enemyOcc != e)   throw new IllegalStateException("Cell already has an Enemy.");
            enemyOcc = e;
        } else {
            throw new IllegalArgumentException("Unsupported occupant type: " + obj.getClass());
        }
    }

    public void removeOccupant(CharacterObject obj) {
        if (obj instanceof Player p) { if (playerOcc == p) playerOcc = null; }
        else if (obj instanceof Enemy e) { if (enemyOcc == e) enemyOcc = null; }
    }

    public void clearOccupants() { playerOcc = null; enemyOcc = null; }

    // --- ASCII symbol (GUI may ignore and draw sprites instead)
    public char symbol() {
        // NEW PRIORITY: show characters first
        if (hasCollision()) return 'C';
        if (hasPlayer())    return 'P';
        if (hasEnemy())     return 'B';
        if (item != null)   return item.symbol();

        // Terrain last (so S/E show when empty)
        return switch (terrain) {
            case WALL    -> 'X';
            case BARRIER -> '#';
            case START   -> 'S';
            case EXIT    -> 'E';
            default      -> ' ';
        };

    }

    @Override public String toString() {
        return "Cell{" + terrain + ",P=" + (playerOcc!=null) + ",E=" + (enemyOcc!=null) + ",item=" + (item!=null) + "}";
    }
}


