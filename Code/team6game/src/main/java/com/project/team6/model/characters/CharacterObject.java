package com.project.team6.model.characters;

import com.project.team6.model.GameObject;
import com.project.team6.model.board.Position;

/**
 * Abstract base for player and enemy characters.
 * Stores position and common movement helpers.
 */
public abstract class CharacterObject extends GameObject {

    /**
     * Creates a character at a starting position.
     *
     * @param position initial location on the board
     */
    protected CharacterObject(Position position) {
        super(position);
    }

    /**
     * Updates the character position after a successful move.
     * Called by the board.
     *
     * @param p new position
     */
    public final void setPosition(Position p) {
        super.setPosition(p);
    }

    /**
     * Returns the ASCII symbol for this character.
     * Used in text based or debug views.
     *
     * @return character symbol
     */
    @Override
    public abstract char symbol();
}
