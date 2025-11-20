package com.project.team6.model;

// This class is the parent for all items a player can step on.
// Other item types will extend this class.

import com.project.team6.model.board.Position;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all world objects.
 * Stores a unique id and a position.
 * Used by characters and collectibles.
 */
public abstract class GameObject {
    /** Unique identifier for this object. */
    private final UUID id = UUID.randomUUID();

    /** Current grid position. */
    private Position position;

    /**
     * Creates an object at a position.
     *
     * @param position starting location
     * @throws NullPointerException if position is null
     */
    protected GameObject(Position position) {
        this.position = Objects.requireNonNull(position);
    }

    /**
     * Returns the unique id of this object.
     *
     * @return UUID value
     */
    public UUID id() { return id; }

    /**
     * Returns the current position.
     *
     * @return grid position
     */
    public Position position() { return position; }

    /**
     * Updates the position.
     * Intended for the board when moving objects.
     *
     * @param position new location
     * @throws NullPointerException if position is null
     */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null");
    }

    /**
     * Single character symbol for debug or text views.
     *
     * @return ASCII character that represents this object
     */
    public abstract char symbol();

    /**
     * Returns a short string with class name and position.
     *
     * @return string summary
     */
    @Override public String toString() {
        return getClass().getSimpleName() + "@" + position;
    }
}
