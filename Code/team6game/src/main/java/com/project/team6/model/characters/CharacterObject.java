package com.project.team6.model.characters;

import com.project.team6.model.GameObject;
import com.project.team6.model.boardUtilities.Board;
import com.project.team6.model.boardUtilities.Direction;
import com.project.team6.model.boardUtilities.Position;
import com.project.team6.model.boardUtilities.Cell;

import java.util.Objects;

/**
 * Base for Player and Enemy. Owns common movement helpers.
 */
public abstract class CharacterObject extends GameObject {

    protected CharacterObject(int x, int y) {
        super(x, y);
    }

    /**
     * Attempt to move one tile in a direction. Returns true if moved.
     * The Board is authoritative for legality/occupancy updates.
     */
    public boolean tryMove(Board board, Direction dir) {
        Objects.requireNonNull(board);
        Position from = position();
        Position to = new Position(from.x() + dir.dx, from.y() + dir.dy);
        if (!board.isInBounds(to)) return false;

        Cell target = board.cellAt(to);
        if (target == null || !target.isEnterableNow()) return false;

        boolean ok = board.moveCharacter(from, to);
        if (ok) setPosition(to);
        return ok;
    }

    /** Character glyph for ASCII view. */
    @Override public abstract char symbol();
}
