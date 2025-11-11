package com.project.team6.model;

// This class is the parent for all items a player can step on.
// Other item types will extend this class.

/** Base type for anything the player can step on and trigger. */
import com.project.team6.model.boardUtilities.Position;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all in-world objects (characters, collectibles).
 * Holds identity and location.
 */
public abstract class GameObject {
    private final UUID id = UUID.randomUUID();
    private Position position;

    protected GameObject(int x, int y) {
        this.position = new Position(x, y);
    }

    public UUID id() { return id; }

    public Position position() { return position; }
    /** Intended to be used by Board when moving objects between cells. */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position);
    }

    /** Single-character ASCII for quick board display; GUI can ignore this. */
    public abstract char symbol();

    @Override public String toString() {
        return getClass().getSimpleName() + "@" + position;
    }
}

