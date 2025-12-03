package com.project.team6.model.board;

import com.project.team6.model.characters.CharacterObject;
import com.project.team6.model.characters.enemies.Enemy;
import com.project.team6.model.characters.Player;
import com.project.team6.model.collectibles.CollectibleObject;

import java.util.Objects;

/**
 * Represents one tile on the board.
 * A cell can hold one player and one enemy at most.
 * It can also hold one item.
 */
public final class Cell {

    /**
     * Types of terrain a cell can have.
     * WALL and BARRIER are not walkable.
     */
    public static enum Terrain { FLOOR, WALL, BARRIER, START, EXIT }

    private final Terrain terrain;
    private CollectibleObject item; // optional
    private Player playerOcc;       // null if none
    private Enemy  enemyOcc;        // null if none

    /**
     * Creates a cell with a terrain type.
     *
     * @param terrain the terrain of this cell
     * @throws NullPointerException if terrain is null
     */
    public Cell(Terrain terrain) { this.terrain = Objects.requireNonNull(terrain); }

    // --- Accessors

    /**
     * Gets the terrain type.
     *
     * @return terrain enum
     */
    public Terrain terrain()                 { return terrain; }

    /**
     * Gets the item in this cell.
     *
     * @return item or null
     */
    public CollectibleObject item()          { return item; }

    /**
     * Sets the item in this cell.
     *
     * @param i item to place, or null to clear
     */
    public void setItem(CollectibleObject i) { this.item = i; }

    /**
     * Gets the player occupant.
     *
     * @return player or null
     */
    public Player player()      { return playerOcc; }

    /**
     * Gets the enemy occupant.
     *
     * @return enemy or null
     */
    public Enemy  enemy()       { return enemyOcc; }

    /**
     * Checks if a player is present.
     *
     * @return true if a player is here
     */
    public boolean hasPlayer()  { return playerOcc != null; }

    /**
     * Checks if an enemy is present.
     *
     * @return true if an enemy is here
     */
    public boolean hasEnemy()   { return enemyOcc  != null; }

    /**
     * Checks if both a player and an enemy are here.
     *
     * @return true if both are present
     */
    public boolean hasCollision(){ return hasPlayer() && hasEnemy(); }

    // --- Terrain

    /**
     * Checks if the terrain can be walked on.
     *
     * @return true for floor, start, or exit
     */
    public boolean isWalkableTerrain() {
        return terrain != Terrain.WALL && terrain != Terrain.BARRIER;
    }

    // --- Occupancy management

    /**
     * Adds a character as an occupant of this cell.
     * Enforces one player and one enemy at most.
     *
     * @param obj character to add
     * @throws IllegalStateException if the slot is already taken
     * @throws IllegalArgumentException if type is not supported
     */
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

    /**
     * Removes a character from this cell if it is present.
     *
     * @param obj character to remove
     */
    public void removeOccupant(CharacterObject obj) {
        if (obj instanceof Player p) { if (playerOcc == p) playerOcc = null; }
        else if (obj instanceof Enemy e) { if (enemyOcc == e) enemyOcc = null; }
    }

    /**
     * Clears both player and enemy from this cell.
     */
    public void clearOccupants() { playerOcc = null; enemyOcc = null; }

    // --- ASCII symbol (GUI may ignore and draw sprites instead)

    /**
     * Returns an ASCII symbol for debugging or text views.
     * Characters take priority over items and terrain.
     *
     * @return symbol representing the current state
     */
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

    /**
     * Returns a brief string with terrain and occupancy flags.
     *
     * @return text summary of the cell
     */
    @Override public String toString() {
        return "Cell{" + terrain + ",P=" + (playerOcc!=null) + ",E=" + (enemyOcc!=null) + ",item=" + (item!=null) + "}";
    }
}
