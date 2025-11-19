package com.project.team6.model.characters;

import com.project.team6.model.GameObject;
import com.project.team6.model.boardUtilities.Position;

/**
 * Base for Player and Enemy. Owns common movement helpers.
 */
public abstract class CharacterObject extends GameObject {
    protected CharacterObject(Position position) {
        super(position);
    }

    /** Board calls this after a successful move to update the character’s position. */
    public final void setPosition(Position p) {
        super.setPosition(p);
    }

    /** Symbol used for ASCII / debug rendering. */
    @Override
    public abstract char symbol();
}
