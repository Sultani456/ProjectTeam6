package com.project.team6.model.characters;

import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;

/**
 * Player character controlled by input.
 * The board still validates movement.
 */
public class Player extends CharacterObject {

    /**
     * Creates a player at a starting position.
     *
     * @param position initial location on the board
     */
    public Player(Position position) { super(position); }

    /**
     * Returns the ASCII symbol for the player.
     *
     * @return 'P'
     */
    @Override public char symbol() { return 'P'; }

    @Override
    public boolean canEnter(Cell cell) {
        if (!cell.isWalkableTerrain()) {return false;}
        return cell.player() == null;
    }
}
