package model.objects.characters;

import model.boardUtilities.Cell;
import model.boardUtilities.Direction;
import model.objects.GameObject;

/**
 * Abstract character that can move on the board.
 */
public abstract class Character extends GameObject {
    protected Direction direction;

    public Character(model.boardUtilities.Position position) {
        super(position);
    }

    /**
     * Moves the character in the given direction if valid.
     */
    public abstract void move(Direction direction);

    /**
     * Checks if the character can move into the target cell.
     */
    public abstract boolean canMoveTo(Cell cell);
}

