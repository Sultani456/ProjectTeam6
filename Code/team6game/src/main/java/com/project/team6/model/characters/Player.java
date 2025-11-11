package com.project.team6.model.characters;

import com.project.team6.model.boardUtilities.Board;
import com.project.team6.model.boardUtilities.Direction;

/**
 * Player controlled by input. Movement is still validated by the Board.
 */
public class Player extends CharacterObject {

    public Player(int x, int y) { super(x, y); }

    /** Convenience wrapper for controller input. */
    public boolean move(Board board, Direction dir) {
        return tryMove(board, dir);
    }

    @Override public char symbol() { return 'P'; }
}
