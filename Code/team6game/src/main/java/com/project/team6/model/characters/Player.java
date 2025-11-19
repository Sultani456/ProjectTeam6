package com.project.team6.model.characters;

import com.project.team6.model.board.Position;

/**
 * Player controlled by input. Movement is still validated by the Board.
 */
public class Player extends CharacterObject {

    public Player(Position position) { super(position); }

    @Override public char symbol() { return 'P'; }
}
